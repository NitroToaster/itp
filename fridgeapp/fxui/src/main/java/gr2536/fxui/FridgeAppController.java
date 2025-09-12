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

public class FridgeAppController {

    @FXML private TextField itemInput;
    @FXML private Button addButton, removeButton, loadButton, saveButton;
    @FXML private ListView<String> fridgeList;

    private final ObservableList<String> items = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        fridgeList.setItems(items);

        addButton.setOnAction(this::onAdd);
        removeButton.setOnAction(this::onRemove);
        loadButton.setOnAction(this::onLoad);
        saveButton.setOnAction(this::onSave);
        itemInput.setOnAction(this::onAdd);

        // Disable "Add" when input is empty/blank
        addButton.disableProperty().bind(
            Bindings.createBooleanBinding(
                () -> itemInput.getText() == null || itemInput.getText().trim().isEmpty(),
                itemInput.textProperty()
            )
        );

        // Disable "Remove" when nothing selected
        removeButton.disableProperty().bind(
            fridgeList.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    // ===== Event handlers wired from FXML (onAction="#...") =====

    @FXML
    private void onAdd(ActionEvent e) {
        String item = itemInput.getText() == null ? "" : itemInput.getText().trim();
        if (item.isEmpty()) return;

        items.add(item);

        itemInput.clear();
    }

    @FXML
    private void onRemove(ActionEvent e) {
        String selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected != null) items.remove(selected);
    }

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

    // ===== Persistence API (easy to unit test) =====

    public void saveToFile(Path file) throws IOException {
        Files.write(file, items, StandardCharsets.UTF_8);
    }

    public void loadFromFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        items.setAll(lines);
    }

    // ===== Dialog helpers =====

    private void showError(String header, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    private void showInfo(String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}