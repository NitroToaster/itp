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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
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

    // Input fields for item, unit, quantity, expiration date.
    //* Item name */
    @FXML private TextField nameField;
    //* Unit type */
    @FXML private TextField unitField;
    //* Quantity spinner */
    @FXML private Spinner<Integer> quantitySpinner;
    //* Expiration date */
    @FXML private DatePicker expirationPicker;

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
        fridgeList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Item item, boolean empty) {

                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    String date = item.getExpirationDate() == null ? "(no date)" : item.getExpirationDate().toString();
                    setText(item.getName() + " - " + item.getQuantity() + " " + item.getUnit() + " - exp: " + date);
                }
            }
        });

        //Spinner setup
        if (quantitySpinner.getValueFactory() == null) {
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
        }

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
        BooleanBinding unitBlank = Bindings.createBooleanBinding(
            () -> unitField.getText() == null || unitField.getText().trim().isEmpty(),
            unitField.textProperty());
        BooleanBinding dateMissing = Bindings.createBooleanBinding(
            () -> expirationPicker.getValue() == null, 
            expirationPicker.valueProperty());

        addButton.disableProperty().bind((nameBlank).or(qtyInvalid).or(unitBlank).or(dateMissing));
        removeButton.disableProperty().bind(fridgeList.getSelectionModel().selectedItemProperty().isNull());
        saveButton.disableProperty().bind(Bindings.isEmpty(items));
        
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
            String unit = unitField.getText().trim();
            LocalDate date = expirationPicker.getValue();

            Item item = new Item(name, qty, unit, date);
            fridge.add(item);
            refreshFromModel();

            nameField.clear();
            unitField.clear();
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

        fridge.remove(selected.getName(), selected.getUnit(), selected.getQuantity());
        refreshFromModel();
    }

    /**
     * Load items from a .txt file in CSV-like format: 
     * {@code name, qty, unit, YYYY-MM-DD}
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
     * {@code name, qty, unit, YYYY-MM-DD}
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