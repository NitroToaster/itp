package gr2536.fxui;

import java.io.File;
import java.time.LocalDate;

import gr2536.core.Fridge;
import gr2536.core.FridgeFileManager;
import gr2536.core.Item;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * JavaFX controller for the Fridge UI.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Wire UI controls to domain logic</li>
 *   <li>Render {@link Item} buckets in a ListView</li>
 *   <li>Basic input validation, and enable/disable logic for buttons</li>
 *   <li>Save/Load items via {@link FridgeFileManager}</li>
 * </ul>
 */
public class FridgeAppController {

    // Input fields for item, quantity, expiration date.
    //* Item name */
    @FXML private TextField nameField;
    //* Quantity spinner */
    @FXML private Spinner<Integer> quantitySpinner;
    //* Expiration date */
    @FXML private DatePicker expirationPicker;
    //* Anchorpane/Background */
    @FXML private AnchorPane root;

    /** Buttons for add/remove/load/save actions. */
    @FXML private Button addButton, removeButton, loadButton, saveButton;
    /** List of items currently in the fridge. */
    @FXML private ListView<Item> fridgeList;

    //* Domain model & Persistence */
    private Fridge fridge = new Fridge();
    private final FridgeFileManager ffm = new FridgeFileManager();

    /** ListView */
    private final ObservableList<Item> items = FXCollections.observableArrayList();

    /**
     * Initializes the UI: cell factory, spinners, event handlers, and validation bindings.
     * Called by the FXMLLoader after FXML fields are injected.
     */
    @FXML
    private void initialize() {

        //ListView setup
        fridgeList.setItems(items);
        fridgeList.setCellFactory(lv -> {
            ListCell<Item> cell = new ListCell<>() {
                @Override protected void updateItem(Item item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String date = item.getExpirationDate() == null ? "(no date)" : item.getExpirationDate().toString();
                        setText(item.getName() + " - " + item.getQuantity() + " - exp: " + date);
                    }
                }
            };

            // Toggle selection when clicking already-selected cell
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (fridgeList.getSelectionModel().getSelectedIndex() == index) {
                        fridgeList.getSelectionModel().clearSelection();
                        e.consume();
                    }
                }
            });

            return cell;
        });

        //Spinner setup
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // Wire events in code
        addButton.setOnAction(this::onAdd);
        removeButton.setOnAction(this::onRemove);
        loadButton.setOnAction(this::onLoad);
        saveButton.setOnAction(this::onSave);

        //Button enable/disable
        BooleanBinding nameBlank = Bindings.createBooleanBinding(
            () -> nameField.getText() == null || nameField.getText().trim().isEmpty(),
            nameField.textProperty());
        BooleanBinding qtyInvalid = Bindings.createBooleanBinding(
            () -> quantitySpinner.getValue() == null || quantitySpinner.getValue() <= 0,
            quantitySpinner.valueProperty());
        BooleanBinding dateMissing = Bindings.createBooleanBinding(
            () -> expirationPicker.getValue() == null, 
            expirationPicker.valueProperty());

        addButton.disableProperty().bind((nameBlank).or(qtyInvalid).or(dateMissing));
        removeButton.disableProperty().bind(fridgeList.getSelectionModel().selectedItemProperty().isNull());
        saveButton.disableProperty().bind(Bindings.isEmpty(items));

        // Clear focus when clicking background
        root.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            javafx.scene.Node hit = e.getPickResult().getIntersectedNode();
            if (!isInsideControl(hit)) {
                root.requestFocus();
                fridgeList.getSelectionModel().clearSelection();
            }
        });

        // Handle ListView selection clearing
        fridgeList.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            Node hit = e.getPickResult().getIntersectedNode();
            ListCell<?> cell = findListCell(hit);

            // Don't clear when using the scrollbar
            boolean hitBar = hasAncestorOfType(hit, javafx.scene.control.ScrollBar.class);
            if (hitBar) return;

            // Clear if clicked blank space
            if (cell == null || cell.isEmpty()) {
                fridgeList.getSelectionModel().clearSelection();
                root.requestFocus();
                e.consume();
            }
        });
        
        //Initial Refresh
        refreshFromModel();
    }

    /**
     * Validates item and adds it to the domain model if valid.
     * Clears field and focuses namefield on success.
     * 
     * @param e action event from button or text field
     * @throws IllegalArgumentException if invalid input.
     */
    @FXML
    private void onAdd(ActionEvent e) {
        
        try {

            String name = nameField.getText().trim();
            int qty = quantitySpinner.getValue();
            LocalDate date = expirationPicker.getValue();

            Item item = new Item(name, qty, date);
            fridge.add(item);
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
     * Remove the currently selected item(s).
     * @param e action event
     */
    @FXML
    private void onRemove(ActionEvent e) {
        Item selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        fridge.remove(selected.getName(), quantitySpinner.getValue());
        quantitySpinner.getValueFactory().setValue(1);
        refreshFromModel();
    }

    /**
     * Load items from a .txt file in CSV-like format: 
     * {@code name, qty, YYYY-MM-DD}
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
                this.fridge = newFridge;
                refreshFromModel();
            } catch (Exception exception) {
                showError("Could not read file.", exception);
            }
        }
    }

    /**
     * Save items to a .txt file in CSV-like format: 
     * {@code name, qty, YYYY-MM-DD}
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
                ffm.saveFridgeData(fridge, f.getAbsolutePath());
            } catch (Exception exception) {
                showError("Could not save file.", exception);
            }
        }
    }

    //* Refresh ListView with items from the fridge (domain). */
    private void refreshFromModel(){
        items.setAll(fridge.listItems());
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