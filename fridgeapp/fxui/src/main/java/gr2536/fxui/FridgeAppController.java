/* Used Github CoPilot to refactor the initialize method into several private methods */

package gr2536.fxui;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import gr2536.core.Fridge;
import gr2536.core.FridgeFileManager;
import gr2536.core.Item;
import gr2536.core.NameMatchMode;
import gr2536.core.SearchCriteria;
import gr2536.core.SearchSort;
import gr2536.fxui.FridgeService;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JavaFX controller for the Fridge UI.
 * <p>
 * Responsibilities:
 * <ul>
 *  <li>Wire UI controls to domain logic</li>
 *  <li>Render {@link Item} buckets in a ListView</li>
 *  <li>Basic input validation, and enable/disable logic for buttons</li>
 *  <li>Save/Load items via {@link FridgeFileManager}</li>
 *  <li>Search and filter items using {@link SearchCriteria}</li>
 * </ul>
 */
public class FridgeAppController {

    // ========== Input fields for item, quantity, expiration date ==========
    /** Item name input field. */
    @FXML
    private TextField nameField;
    /** Quantity spinner for add operations. */
    @FXML
    private Spinner<Integer> quantitySpinner;
    /** Expiration date picker. */
    @FXML
    private DatePicker expirationPicker;
    /** Root pane/Background. */
    @FXML
    private AnchorPane root;

    // ========== Search controls ==========
    /** Search text field for filtering by name. */
    @FXML
    private TextField searchField;
    /** Combo box for selecting name match mode. */
    @FXML
    private ComboBox<NameMatchMode> searchModeCombo;
    /** Combo box for selecting sort order. */
    @FXML
    private ComboBox<SearchSort> searchSortCombo;
    /** Button to execute search. */
    @FXML
    private Button searchButton;
    /** Button to clear search and show all items. */
    @FXML
    private Button clearSearchButton;

    // ========== Filter controls ==========
    /** Spinner for minimum quantity filter. */
    @FXML
    private Spinner<Integer> filterMinQuantitySpinner;
    /** Spinner for maximum quantity filter. */
    @FXML
    private Spinner<Integer> filterMaxQuantitySpinner;
    /** Date picker for filtering items expiring from this date onwards. */
    @FXML
    private DatePicker filterExpirationFromPicker;
    /** Date picker for filtering items expiring until this date. */
    @FXML
    private DatePicker filterExpirationUntilPicker;
    /** Checkbox to include items without expiration date in filter results. */
    @FXML
    private CheckBox filterIncludeUnknownCheck;
    /** Button to apply filter criteria. */
    @FXML
    private Button applyFilterButton;
    /** Button to clear filter criteria. */
    @FXML
    private Button clearFilterButton;

    // ========== Action buttons ==========
    /** Button to add item with quantity from spinner. */
    @FXML
    private Button addButton;
    /** Button to add exactly one unit of item. */
    @FXML
    private Button addOneButton;
    /** Button to remove one unit of selected item. */
    @FXML
    private Button removeOneButton;
    /** Button to remove all units of selected item. */
    @FXML
    private Button removeAllButton;
    /** Button to load fridge data from file. */
    @FXML
    private Button loadButton;
    /** Button to save fridge data to file. */
    @FXML
    private Button saveButton;
    /** Button to navigate to shopping list. */
    @FXML
    private Button shoppingListButton;

    /** List view displaying items in the fridge. */
    @FXML
    private ListView<Item> fridgeList;

    // ========== Domain model & Persistence ==========
    /** File manager for persistence. */
    private final FridgeFileManager ffm = new FridgeFileManager();

    /** ListView backing data. */
    private final ObservableList<Item> items = FXCollections.observableArrayList();

    // ========== State management ==========
    /**
     * Current search criteria. Null means showing all items without filters.
     */
    private SearchCriteria currentSearchCriteria = null;

    /**
     * Flag to track if we're in filtered/search view.
     */
    private boolean isFiltered = false;

    /**
     * Initializes the UI: delegates setup to helper methods for clarity and
     * maintainability.
     * Called by the FXMLLoader after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        setupListView();
        setupSpinner();
        setupSearchControls();
        setupFilterControls();
        setupButtonEvents();
        setupButtonBindings();
        setupBackgroundClickHandling();
        setupListViewSelectionClearing();
        refreshFromModel();
    }

    /** Sets up the ListView cell factory and selection toggling. */
    private void setupListView() {
        fridgeList.setItems(items);
        fridgeList.setCellFactory(lv -> createItemCell());
    }

    /** Creates a custom ListCell for displaying Item details. */
    private ListCell<Item> createItemCell() {
        ListCell<Item> cell = new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String date = item.getExpirationDate() == null ? "(no date)" : item.getExpirationDate().toString();
                    setText(item.getName() + " - " + item.getQuantity() + " - exp: " + date);
                }
            }
        };
        cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> handleCellToggle(cell, e));
        return cell;
    }

    /** Handles toggling selection when clicking an already-selected cell. */
    private void handleCellToggle(ListCell<Item> cell, javafx.scene.input.MouseEvent e) {
        if (!cell.isEmpty()) {
            int index = cell.getIndex();
            if (fridgeList.getSelectionModel().getSelectedIndex() == index) {
                fridgeList.getSelectionModel().clearSelection();
                e.consume();
            }
        }
    }

    /** Sets up the quantity spinner for add operations. */
    private void setupSpinner() {
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
    }

    /**
     * Sets up search-related controls: combo boxes and button bindings.
     * Populates the search mode and sort combo boxes with enum values.
     */
    private void setupSearchControls() {
        // Populate search mode combo box
        searchModeCombo.setItems(FXCollections.observableArrayList(NameMatchMode.values()));
        searchModeCombo.setValue(NameMatchMode.CONTAINS);

        // Populate search sort combo box
        searchSortCombo.setItems(FXCollections.observableArrayList(SearchSort.values()));
        searchSortCombo.setValue(SearchSort.DEFAULT);

        // Bind button states
        BooleanBinding searchFieldEmpty = Bindings.createBooleanBinding(
            () -> searchField.getText() == null || searchField.getText().trim().isEmpty(),
            searchField.textProperty()
        );

        // Search button enabled when search field has text
        searchButton.disableProperty().bind(searchFieldEmpty);

        // Clear search button enabled when in filtered mode
        clearSearchButton.disableProperty().bind(
            Bindings.createBooleanBinding(() -> !isFiltered, 
                fridgeList.itemsProperty())
        );

        // Wire events
        searchButton.setOnAction(this::onSearch);
        clearSearchButton.setOnAction(this::onClearSearch);
    }

    /**
     * Sets up filter controls: spinners, date pickers, and checkbox.
     * Initializes default values for filter criteria.
     */
    private void setupFilterControls() {
        // Setup quantity spinners for filtering
        filterMinQuantitySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 0)
        );
        filterMaxQuantitySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 99, 99)
        );

        // Default: include items without expiration date
        filterIncludeUnknownCheck.setSelected(true);

        // Wire events
        applyFilterButton.setOnAction(this::onApplyFilter);
        clearFilterButton.setOnAction(this::onClearFilter);
    }

    /** Wires button click events to their handlers. */
    private void setupButtonEvents() {
        addButton.setOnAction(this::onAdd);
        addOneButton.setOnAction(this::onAddOne);
        removeOneButton.setOnAction(this::onRemoveOne);
        removeAllButton.setOnAction(this::onRemoveAll);
        loadButton.setOnAction(this::onLoad);
        saveButton.setOnAction(this::onSave);
        shoppingListButton.setOnAction(this::onNavigateToShoppingList);
    }

    /** Sets up enable/disable bindings for buttons based on input validation. */
    private void setupButtonBindings() {
        // Name validation
        BooleanBinding nameBlank = Bindings.createBooleanBinding(
            () -> nameField.getText() == null || nameField.getText().trim().isEmpty(),
            nameField.textProperty()
        );

        // Quantity validation
        BooleanBinding qtyInvalid = Bindings.createBooleanBinding(
            () -> quantitySpinner.getValue() == null || quantitySpinner.getValue() <= 0,
            quantitySpinner.valueProperty()
        );

        // Date validation
        BooleanBinding dateMissing = Bindings.createBooleanBinding(
            () -> expirationPicker.getValue() == null,
            expirationPicker.valueProperty()
        );

        // Selection validation
        BooleanBinding noSelection = fridgeList.getSelectionModel()
            .selectedItemProperty().isNull();

        // Add buttons: require name and date
        addButton.disableProperty().bind(nameBlank.or(qtyInvalid).or(dateMissing));
        addOneButton.disableProperty().bind(nameBlank.or(dateMissing));

        // Remove buttons: require item selection
        removeOneButton.disableProperty().bind(noSelection);
        removeAllButton.disableProperty().bind(noSelection);

        // Save button: require at least one item
        saveButton.disableProperty().bind(Bindings.isEmpty(items));
    }

    /** Handles background clicks to clear focus and selection. */
    private void setupBackgroundClickHandling() {
        root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            javafx.scene.Node hit = e.getPickResult().getIntersectedNode();
            if (!isInsideControl(hit)) {
                root.requestFocus();
                fridgeList.getSelectionModel().clearSelection();
            }
        });
    }

    /** Handles ListView selection clearing when clicking blank space. */
    private void setupListViewSelectionClearing() {
        fridgeList.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            Node hit = e.getPickResult().getIntersectedNode();
            ListCell<?> cell = findListCell(hit);
            boolean hitBar = hasAncestorOfType(hit, javafx.scene.control.ScrollBar.class);
            if (hitBar) {
                return;
            }
            if (cell == null || cell.isEmpty()) {
                fridgeList.getSelectionModel().clearSelection();
                root.requestFocus();
                e.consume();
            }
        });
    }

    /**
     * Executes search based on current search criteria.
     * Updates the ListView with filtered and sorted results.
     * 
     * @param e ActionEvent from search button
     */
    @FXML
    private void onSearch(ActionEvent e) {
        try {
            String query = searchField.getText().trim();
            NameMatchMode mode = searchModeCombo.getValue();
            SearchSort sort = searchSortCombo.getValue();

            // Build search criteria with current filter values (if any)
            Integer minQty = filterMinQuantitySpinner.getValue() > 0 
                ? filterMinQuantitySpinner.getValue() 
                : null;
            Integer maxQty = filterMaxQuantitySpinner.getValue() < 99 
                ? filterMaxQuantitySpinner.getValue() 
                : null;
            LocalDate fromDate = filterExpirationFromPicker.getValue();
            LocalDate untilDate = filterExpirationUntilPicker.getValue();
            boolean includeUnknown = filterIncludeUnknownCheck.isSelected();

            // Create search criteria
            currentSearchCriteria = new SearchCriteria(
                query, mode, minQty, maxQty, fromDate, untilDate, includeUnknown, sort
            );

            // Execute search and update view
            List<Item> results = getFridge().search(currentSearchCriteria);
            items.setAll(results);
            isFiltered = true;

        } catch (IllegalArgumentException exception) {
            showError("Invalid search criteria", exception);
        }
    }

    /**
     * Clears all search and filter criteria, returning to full list view.
     * Resets all search and filter controls to their default values.
     * 
     * @param e ActionEvent from clear button
     */
    @FXML
    private void onClearSearch(ActionEvent e) {
        // Reset search fields
        searchField.clear();
        searchModeCombo.setValue(NameMatchMode.CONTAINS);
        searchSortCombo.setValue(SearchSort.DEFAULT);

        // Reset filter fields
        filterMinQuantitySpinner.getValueFactory().setValue(0);
        filterMaxQuantitySpinner.getValueFactory().setValue(99);
        filterExpirationFromPicker.setValue(null);
        filterExpirationUntilPicker.setValue(null);
        filterIncludeUnknownCheck.setSelected(true);

        // Clear criteria and refresh
        currentSearchCriteria = null;
        isFiltered = false;
        refreshFromModel();
    }

    /**
     * Applies filter criteria without requiring search text.
     * Useful for filtering by quantity or expiration date ranges.
     * 
     * @param e ActionEvent from apply filter button
     */
    @FXML
    private void onApplyFilter(ActionEvent e) {
        try {
            // Get filter values
            Integer minQty = filterMinQuantitySpinner.getValue() > 0 
                ? filterMinQuantitySpinner.getValue() 
                : null;
            Integer maxQty = filterMaxQuantitySpinner.getValue() < 99 
                ? filterMaxQuantitySpinner.getValue() 
                : null;
            LocalDate fromDate = filterExpirationFromPicker.getValue();
            LocalDate untilDate = filterExpirationUntilPicker.getValue();
            boolean includeUnknown = filterIncludeUnknownCheck.isSelected();

            // Preserve search query if exists
            String query = searchField.getText().trim();
            String searchQuery = query.isEmpty() ? null : query;
            NameMatchMode mode = searchModeCombo.getValue();
            SearchSort sort = searchSortCombo.getValue();

            // Create search criteria with filters
            currentSearchCriteria = new SearchCriteria(
                searchQuery, mode, minQty, maxQty, fromDate, untilDate, includeUnknown, sort
            );

            // Execute filter and update view
            List<Item> results = getFridge().search(currentSearchCriteria);
            items.setAll(results);
            isFiltered = true;

        } catch (IllegalArgumentException exception) {
            showError("Invalid filter criteria", exception);
        }
    }

    /**
     * Clears only the filter criteria while preserving search text/mode/sort.
     * Useful when user wants to search without quantity/date constraints.
     * 
     * @param e ActionEvent from clear filter button
     */
    @FXML
    private void onClearFilter(ActionEvent e) {
        // Reset only filter fields
        filterMinQuantitySpinner.getValueFactory().setValue(0);
        filterMaxQuantitySpinner.getValueFactory().setValue(99);
        filterExpirationFromPicker.setValue(null);
        filterExpirationUntilPicker.setValue(null);
        filterIncludeUnknownCheck.setSelected(true);

        // Re-apply search without filters if search was active
        if (!searchField.getText().trim().isEmpty()) {
            onSearch(e);
        } else {
            onClearSearch(e);
        }
    }

    /**
     * Adds the specified quantity of an item to the fridge.
     * Clears fields and focuses name field on success.
     * 
     * @param e ActionEvent from addAll button
     * @throws IllegalArgumentException if invalid input
     */
    @FXML
    private void onAdd(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            int qty = quantitySpinner.getValue();
            LocalDate date = expirationPicker.getValue();

            Item item = new Item(name, qty, date);
            getFridge().add(item);
            refreshFromModel();

            nameField.clear();
            quantitySpinner.getValueFactory().setValue(1);
            expirationPicker.setValue(null);

            nameField.requestFocus();

        } catch (IllegalArgumentException exception) {
            showError("Invalid input", exception);
        }
    }

    /**
     * Adds exactly one unit of the specified item to the fridge.
     * Similar to onAdd but always adds quantity of 1, ignoring spinner value.
     * Useful for quickly adding single items.
     * 
     * @param e ActionEvent from addOne button
     * @throws IllegalArgumentException if invalid input
     */
    @FXML
    private void onAddOne(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            LocalDate date = expirationPicker.getValue();

            // Always add quantity of 1
            Item item = new Item(name, 1, date);
            getFridge().add(item);

            // Refresh view (respecting current filters if active)
            refreshFromModel();

            // Clear input fields
            nameField.clear();
            expirationPicker.setValue(null);

            nameField.requestFocus();

        } catch (IllegalArgumentException exception) {
            showError("Invalid input", exception);
        }
    }

    /**
     * Removes exactly one unit of the selected item from the fridge.
     * Removes from the oldest expiration date first (FIFO).
     * 
     * @param e ActionEvent from removeOne button
     */
    @FXML
    private void onRemoveOne(ActionEvent e) {
        Item selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        // Remove exactly 1 unit
        getFridge().remove(selected.getName(), 1);

        // Refresh view (respecting current filters if active)
        refreshFromModel();
    }

    /**
     * Removes all units of the selected item from the fridge.
     * Clears the item completely, regardless of expiration dates.
     * 
     * @param e ActionEvent from removeAll button
     */
    @FXML
    private void onRemoveAll(ActionEvent e) {
        Item selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        // Get total quantity and remove it all
        int totalQuantity = getFridge().getQuantity(selected.getName());
        getFridge().remove(selected.getName(), totalQuantity);

        // Refresh view (respecting current filters if active)
        refreshFromModel();
    }

    /**
     * Load items from a .txt file in CSV-like format:
     * {@code name, qty, YYYY-MM-DD}
     * 
     * @param e action event
     */
    @FXML
    private void onLoad(ActionEvent e) {
        Window window = fridgeList.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Open fridge file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File f = fc.showOpenDialog(window);

        if (f != null) {
            try {
                Fridge newFridge = new Fridge();
                ffm.readFridgeData(newFridge, f.getAbsolutePath());
                FridgeService.setFridge(newFridge);
                
                // Clear any active filters when loading new data
                currentSearchCriteria = null;
                isFiltered = false;
                refreshFromModel();
            } catch (Exception exception) {
                showError("Could not read file.", exception);
            }
        }
    }

    /**
     * Save items to a .txt file in CSV-like format:
     * {@code name, qty, YYYY-MM-DD}
     * 
     * @param e action event
     */
    @FXML
    private void onSave(ActionEvent e) {
        Window window = fridgeList.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Save fridge file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File f = fc.showSaveDialog(window);

        if (f != null) {
            try {
                ffm.saveFridgeData(getFridge(), f.getAbsolutePath());
            } catch (Exception exception) {
                showError("Could not save file.", exception);
            }
        }
    }

    /**
     * Navigate to the Shopping List interface.
     * @param e action event
     */
    @FXML
    private void onNavigateToShoppingList(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/ShoppingList.fxml"));
            Parent shoppingRoot = loader.load();
            
            Scene shoppingScene = new Scene(shoppingRoot);
            shoppingScene.getStylesheets().add(getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());
            
            Stage stage = (Stage) shoppingListButton.getScene().getWindow();
            stage.setScene(shoppingScene);
            stage.setTitle("Shopping List");
            
        } catch (Exception exception) {
            showError("Navigation Error", new RuntimeException("Could not load Shopping List interface: " + exception.getMessage()));
        }
    }

    /**
     * Refreshes ListView with items from the fridge.
     * If search/filter is active, re-applies the current criteria.
     * Otherwise, shows all items in default order.
     */
    private void refreshFromModel() {
        if (isFiltered && currentSearchCriteria != null) {
            // Re-apply current search/filter criteria
            items.setAll(getFridge().search(currentSearchCriteria));
        } else {
            // Show all items
            items.setAll(getFridge().listItems());
        }
    }

    /**
     * Checks if the clicked node is inside any Control.
     * 
     * @param n the node to check
     * @return true if inside a control, false otherwise
     */
    private boolean isInsideControl(javafx.scene.Node n) {
        while (n != null) {
            if (n instanceof javafx.scene.control.Control) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }

    /**
     * Checks if a node has an ancestor of the specified type.
     * 
     * @param n the node to check
     * @param type the ancestor type to look for
     * @return true if ancestor found, false otherwise
     */
    private boolean hasAncestorOfType(javafx.scene.Node n, Class<?> type) {
        while (n != null) {
            if (type.isInstance(n)) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }

    /**
     * Finds the nearest ListCell ancestor of the given node.
     * 
     * @param n the node to start from
     * @return the ListCell ancestor or null if not found
     */
    private ListCell<?> findListCell(Node n) {
        while (n != null) {
            if (n instanceof ListCell) {
                return (ListCell<?>) n;
            }
            n = n.getParent();
        }
        return null;
    }

    /**
     * Show an error alert with the given header and exception message.
     * 
     * @param header the error header text
     * @param exception the exception containing the error message
     */
    private void showError(String header, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    /**
     * Gets the shared Fridge instance from FridgeService.
     * 
     * @return the current Fridge instance
     */
    private Fridge getFridge() {
        return FridgeService.getFridge();
    }
}