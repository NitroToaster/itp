package gr2536.fxui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * JavaFX controller for the Fridge UI.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Wire UI controls and event handlers</li>
 *   <li>Keep a list of items shown in the {@link ListView}</li>
 *   <li>Basic enable/disable logic for buttons</li>
 *   <li><strong>TEMP</strong>: simple file load/save so the UI can run before the backend is ready</li>
 * </ul>
 * 
 */
public class FridgeAppController {

    /** Text field for entering a new item. */
    @FXML private TextField itemInput;
    /** Buttons for add/remove/load/save actions. */
    @FXML private Button addButton, removeButton, loadButton, saveButton;
    /** List of items currently in the fridge. */
    @FXML private ListView<String> fridgeList;

    /**
     * <strong>TEMP (until backend)</strong>:
     * In-memory model used by the UI.
     */
    private final ObservableList<String> items = FXCollections.observableArrayList();

    /**
     * Called by the FXMLLoader after FXML fields are injected.
     * Wires button/field actions and binds enable/disable state.
     */
    @FXML
    private void initialize() {
        fridgeList.setItems(items);

        // Wire events in code
        addButton.setOnAction(this::onAdd);
        removeButton.setOnAction(this::onRemove);
        loadButton.setOnAction(this::onLoad);
        saveButton.setOnAction(this::onSave);
        itemInput.setOnAction(this::onAdd);

        // Disable "Add" when input is blank
        addButton.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> itemInput.getText() == null || itemInput.getText().trim().isEmpty(),
                itemInput.textProperty()
            )
        );

        // Disable "Remove" when nothing is selected
        removeButton.disableProperty().bind(
            fridgeList.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    /**
     * Add the item from the text field to the list.
     * @param e action event from button or text field
     */
    @FXML
    private void onAdd(ActionEvent e) {
        String item = itemInput.getText() == null ? "" : itemInput.getText().trim();
        if (item.isEmpty()) return;
        items.add(item);
        itemInput.clear();
    }

    /**
     * Remove the currently selected item.
     * @param e action event
     */
    @FXML
    private void onRemove(ActionEvent e) {
        String selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected != null) items.remove(selected);
    }

    /**
     * <strong>TEMP (until backend)</strong>:
     * Show a file chooser and load items from a plain text file. One item per line.
     * @param e action event
     */
    @FXML
    private void onLoad(ActionEvent e) {
        Window window = fridgeList.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Open fridge file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        var f = fc.showOpenDialog(window);

        if (f != null) {
            try {
                loadFromFile(f.toPath());
            } catch (IOException exception) {
                showError("Could not read file.", exception);
            }
        }
    }

    /**
     * <strong>TEMP (until backend)</strong>:
     * Show a file chooser and save items to a plain text file. One item per line.
     * @param e action event
     */
    @FXML
    private void onSave(ActionEvent e) {
        Window window = fridgeList.getScene().getWindow();
        FileChooser fc = new FileChooser();
        fc.setTitle("Save fridge file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        var f = fc.showSaveDialog(window);

        if (f != null) {
            try {
                saveToFile(f.toPath());
            } catch (IOException exception) {
                showError("Could not save file.", exception);
            }
        }
    }

    /**
     * <strong>TEMP (until backend)</strong>:
     * Save the current items to a UTF-8 text file. One line per item.
     * @param file path to write
     * @throws IOException if writing fails
     */
    public void saveToFile(Path file) throws IOException {
        Files.write(file, items, StandardCharsets.UTF_8);
    }

    /**
     * <strong>TEMP (until backend)</strong>:
     * Load items from a UTF-8 text file. One line per item.
     * @param file path to read
     * @throws IOException if reading fails
     */
    public void loadFromFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        items.setAll(lines);
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