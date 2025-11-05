package gr2536.fxui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import gr2536.core.item.Item;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * JavaFX controller for the Shopping List UI.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Manage shopping list items</li>
 *   <li>Provide functionality to add/remove items</li>
 *   <li>Support bulk operations (add all to inventory, remove all)</li>
 *   <li>Navigate back to Fridge interface</li>
 * </ul>
 */
public class ShoppingListController {

    // Input fields for item and quantity
    @FXML private TextField itemField;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private AnchorPane root;

    // Buttons for various actions
    @FXML private Button addItemButton, removeItemButton, addAllToInventoryButton, removeAllButton, backToFridgeButton;
    
    // Sidebar navigation
    @FXML private Button toggleSidebarButton;
    @FXML private Button showSidebarButton;
    @FXML private Button navFridgeButton;
    @FXML private javafx.scene.layout.VBox sidebar;
    
    // List of shopping items
    @FXML private ListView<ShoppingItem> shoppingList;

    // Shopping list data
    private final ObservableList<ShoppingItem> items = FXCollections.observableArrayList();

    /**
     * Simple class to represent a shopping list item with name and quantity.
     */
    public static class ShoppingItem {
        private final String name;
        private final int quantity;

        public ShoppingItem(String name, int quantity) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Item name cannot be empty");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            this.name = name.trim();
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }

        @Override
        public String toString() {
            return name + " - " + quantity;
        }

        @Override
        public boolean equals(Object obj){
            if (this == obj) return true;
            if (!(obj instanceof ShoppingItem)) return false;
            ShoppingItem other = (ShoppingItem) obj;
            return name.equals(other.name) && quantity == other.quantity;
        }

        @Override
        public int hashCode() {
            return name.hashCode() * 31 + quantity;
        }
    }

    /**
     * Initializes the UI: cell factory, spinners, event handlers, and validation bindings.
     * Called by the FXMLLoader after FXML fields are injected.
     */
    @FXML
    private void initialize() {

        // ListView setup
        shoppingList.setItems(items);

        //Load persisted items from service class
        items.setAll(ShoppingListService.getItems());

        shoppingList.setCellFactory(lv -> {
            ListCell<ShoppingItem> cell = new ListCell<>() {
                @Override
                protected void updateItem(ShoppingItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };

            // Toggle selection when clicking already-selected cell
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (shoppingList.getSelectionModel().getSelectedIndex() == index) {
                        shoppingList.getSelectionModel().clearSelection();
                        e.consume();
                    }
                }
            });

            return cell;
        });

        // Spinner setup
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // Wire events
        addItemButton.setOnAction(this::onAddItem);
        removeItemButton.setOnAction(this::onRemoveItem);
        addAllToInventoryButton.setOnAction(this::onAddAllToInventory);
        removeAllButton.setOnAction(this::onRemoveAll);
        if (backToFridgeButton != null) {
            backToFridgeButton.setOnAction(e -> navigateToFridge());
        }
        
        // Setup sidebar navigation
        setupSidebarEvents();

        // Button enable/disable logic
        BooleanBinding itemBlank = Bindings.createBooleanBinding(
            () -> itemField.getText() == null || itemField.getText().trim().isEmpty(),
            itemField.textProperty());
        BooleanBinding qtyInvalid = Bindings.createBooleanBinding(
            () -> quantitySpinner.getValue() == null || quantitySpinner.getValue() <= 0,
            quantitySpinner.valueProperty());

        addItemButton.disableProperty().bind(itemBlank.or(qtyInvalid));
        removeItemButton.disableProperty().bind(shoppingList.getSelectionModel().selectedItemProperty().isNull());
        addAllToInventoryButton.disableProperty().bind(Bindings.isEmpty(items));
        removeAllButton.disableProperty().bind(Bindings.isEmpty(items));

        // Clear focus when clicking background
        root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            javafx.scene.Node hit = e.getPickResult().getIntersectedNode();
            if (!isInsideControl(hit)) {
                root.requestFocus();
                shoppingList.getSelectionModel().clearSelection();
            }
        });

        // Handle ListView selection clearing
        shoppingList.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            Node hit = e.getPickResult().getIntersectedNode();
            ListCell<?> cell = findListCell(hit);

            // Don't clear when using the scrollbar
            boolean hitBar = hasAncestorOfType(hit, javafx.scene.control.ScrollBar.class);
            if (hitBar) return;

            // Clear if clicked blank space
            if (cell == null || cell.isEmpty()) {
                shoppingList.getSelectionModel().clearSelection();
                root.requestFocus();
                e.consume();
            }
        });
    }

    /**
     * Setup sidebar navigation button event handlers.
     */
    private void setupSidebarEvents() {
        if (toggleSidebarButton != null) {
            toggleSidebarButton.setOnAction(this::onToggleSidebar);
        }
        if (showSidebarButton != null) {
            showSidebarButton.setOnAction(this::onToggleSidebar);
        }
        // navFridgeButton handler is set in FXML with onAction="#onNavigateToFridge"
    }

    /**
     * Toggles the sidebar visibility.
     */
    @FXML
    private void onToggleSidebar(ActionEvent e) {
        if (sidebar == null || toggleSidebarButton == null) {
            return;
        }

        boolean currentlyVisible = sidebar.isManaged() && sidebar.isVisible();
        sidebar.setManaged(!currentlyVisible);
        sidebar.setVisible(!currentlyVisible);

        // Use chevron arrows: ‹ to collapse (hide), › to expand (show)
        toggleSidebarButton.setText(currentlyVisible ? "›" : "‹");

        // Show/hide the hamburger menu button
        if (showSidebarButton != null) {
            showSidebarButton.setVisible(currentlyVisible);
            showSidebarButton.setManaged(currentlyVisible);
        }
    }

    /**
     * Navigate to shopping list view (current view - no action needed).
     */
    @FXML
    private void onNavigateToShoppingList(ActionEvent e) {
        // Already on shopping list - no action needed
    }

    /**
     * Load data from file (placeholder for future implementation).
     */
    @FXML
    private void onLoad(ActionEvent e) {
        // TODO: Implement load functionality for shopping list if needed
        showError("Not Implemented", new UnsupportedOperationException("Load data not yet implemented for shopping list"));
    }

    /**
     * Add a new item to the shopping list.
     */
    @FXML
    private void onAddItem(ActionEvent e) {
        try {
            String name = itemField.getText().trim();
            int qty = quantitySpinner.getValue();

            ShoppingItem item = new ShoppingItem(name, qty);
            items.add(item);
            ShoppingListService.addItem(item);

            // Clear fields
            itemField.clear();
            quantitySpinner.getValueFactory().setValue(1);

            itemField.requestFocus();

        } catch (IllegalArgumentException exception) {
            showError("Invalid input", exception);
        }
    }

    /**
     * Remove the selected item from the shopping list.
     */
    @FXML
    private void onRemoveItem(ActionEvent e) {
        ShoppingItem selected = shoppingList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            items.remove(selected);
            ShoppingListService.removeItem(selected);
        }
    }

    /**
     * Add all shopping list items to the inventory with default expiration dates.
     * Items are added with an expiration date of 7 days from today.
     */
    @FXML
    private void onAddAllToInventory(ActionEvent e) {
        if (items.isEmpty()) return;

        try {
            List<String> addedItems = new ArrayList<>();
            LocalDate defaultExpiration = LocalDate.now().plusDays(7);
            
            // Add each shopping item to the fridge
            for (ShoppingItem shoppingItem : items) {
                Item fridgeItem = new Item(shoppingItem.getName(), shoppingItem.getQuantity(), defaultExpiration);
                FridgeService.getFridge().add(fridgeItem);
                addedItems.add(shoppingItem.getName() + " (" + shoppingItem.getQuantity() + ")");
            }

            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Added to Inventory");
            alert.setHeaderText("Successfully added items to fridge:");
            alert.setContentText(String.join("\n", addedItems) + 
                "\n\nDefault expiration: " + defaultExpiration);
            alert.showAndWait();

            // Clear the shopping list after adding to inventory
            items.clear();
            ShoppingListService.clear();
            
        } catch (Exception exception) {
            showError("Failed to add items to inventory", exception);
        }
    }

    /**
     * Remove all items from the shopping list.
     */
    @FXML
    private void onRemoveAll(ActionEvent e) {
        items.clear();
        ShoppingListService.clear();
    }

    /**
     * Navigate back to the Fridge interface (called from sidebar button).
     */
    @FXML
    private void onNavigateToFridge(ActionEvent e) {
        navigateToFridge();
    }

    /**
     * Navigate back to the Fridge interface.
     */
    public void navigateToFridge() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/FridgeApp.fxml"));
            Parent fridgeRoot = loader.load();
            
            Scene fridgeScene = new Scene(fridgeRoot);
            fridgeScene.getStylesheets().add(getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());
            
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(fridgeScene);
            stage.setTitle("Fridge App");
            
        } catch (Exception e) {
            showError("Navigation Error", new RuntimeException("Could not load Fridge interface: " + e.getMessage()));
        }
    }

    /**
     * Checks if the clicked node is inside any Control.
     */
    private boolean isInsideControl(javafx.scene.Node n) {
        while (n != null) {
            if (n instanceof javafx.scene.control.Control) return true;
            n = n.getParent();
        }
        return false;
    }

    /**
     * Checks if a node has an ancestor of the specified type.
     */
    private boolean hasAncestorOfType(javafx.scene.Node n, Class<?> type) {
        while (n != null) {
            if (type.isInstance(n)) return true;
            n = n.getParent();
        }
        return false;
    }

    /**
     * Finds the nearest ListCell ancestor of the given node.
     */
    private ListCell<?> findListCell(Node n) {
        while (n != null) {
            if (n instanceof ListCell) return (ListCell<?>) n;
            n = n.getParent();
        }
        return null;
    }

    /**
     * Show an error alert with the given header and exception message.
     */
    private void showError(String header, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}