package gr2536.fxui;

import javafx.event.ActionEvent;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import gr2536.fxui.ShoppingListController.ShoppingItem;
import java.util.HashSet;
import java.util.Set;

public class RecipeDetailController {

    @FXML private AnchorPane root;
    @FXML private Label titleLabel;
    @FXML private ImageView imageView;
    @FXML private Label metaLabel;
    @FXML private ListView<String> ingredientsList;
    @FXML private TextArea instructionsArea;

    private final RecipeService service = new RecipeService();
    @FXML private javafx.scene.control.CheckBox hideAvailableCheck;
    private String recipeId;
    private Set<String> missingNamesLower = new HashSet<>();

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void load() {
        Task<RecipeModels.RecipeDetails> task = new Task<>() {
            @Override protected RecipeModels.RecipeDetails call() {
                return service.details(recipeId);
            }
        };
        task.setOnSucceeded(ev -> {
            var d = task.getValue();
            titleLabel.setText(d.title());
            if (d.image() != null && !d.image().isBlank()) {
                imageView.setImage(new Image(d.image(), true));
            }
            int totalIng = d.ingredients() == null ? 0 : d.ingredients().size();
            metaLabel.setText(String.format("%s • %s • You have %d of %d", nullSafe(d.category()), nullSafe(d.area()), Math.max(0, totalIng - computeMissingNames(d).size()), totalIng));
            var fridge = FridgeService.getFridge();
            missingNamesLower.clear();
            var items = d.ingredients().stream().map(i -> {
                String name = i.name() == null ? "" : i.name().trim();
                boolean have = fridge.getQuantity(name) > 0;
                if (!have && !name.isBlank()) missingNamesLower.add(name.toLowerCase());
                return name + (i.measure() == null || i.measure().isBlank() ? "" : " - " + i.measure());
            }).toList();
            ingredientsList.getItems().setAll(items);
            ingredientsList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
                @Override protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    if (empty || text == null) {
                        setText(null);
                        getStyleClass().removeAll("ingredient-have", "ingredient-missing");
                    } else {
                        setText(text);
                        String name = text.contains(" - ") ? text.substring(0, text.indexOf(" - ")) : text;
                        boolean missing = missingNamesLower.contains(name.toLowerCase());
                        getStyleClass().removeAll("ingredient-have", "ingredient-missing");
                        getStyleClass().add(missing ? "ingredient-missing" : "ingredient-have");
                    }
                }
            });
            instructionsArea.setText(d.instructions());
            if (hideAvailableCheck != null) {
                hideAvailableCheck.selectedProperty().addListener((obs, was, isNow) -> applyHideAvailable());
            }
        });
        task.setOnFailed(ev -> {
            Throwable e = task.getException();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText("Failed to load recipe");
            a.setContentText(String.valueOf(e == null ? "Unknown error" : e.getMessage()));
            a.showAndWait();
        });
        new Thread(task, "recipe-detail-load").start();
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private java.util.Set<String> computeMissingNames(RecipeModels.RecipeDetails d) {
        var set = new java.util.HashSet<String>();
        var fridge = FridgeService.getFridge();
        if (d.ingredients() != null) {
            for (var i : d.ingredients()) {
                String name = i.name() == null ? "" : i.name().trim();
                boolean have = fridge.getQuantity(name) > 0;
                if (!have && !name.isBlank()) set.add(name.toLowerCase());
            }
        }
        return set;
    }

    private void applyHideAvailable() {
        boolean hide = hideAvailableCheck != null && hideAvailableCheck.isSelected();
        if (!hide) {
            // reset by re-setting current items to include all ingredients; easiest is to re-run load()
            if (recipeId != null) load();
            return;
        }
        var current = new java.util.ArrayList<String>(ingredientsList.getItems());
        var filtered = current.stream()
            .filter(text -> {
                String name = text.contains(" - ") ? text.substring(0, text.indexOf(" - ")) : text;
                return missingNamesLower.contains(name.toLowerCase());
            })
            .toList();
        ingredientsList.getItems().setAll(filtered);
    }

    @FXML
    private void onBack(ActionEvent e) {
        try {
            UiUtil.switchScene(root, "/gr2536/fxui/Recipe.fxml", "Recipes");
        } catch (Exception ex) {
            UiUtil.showError("Navigation Error", "Could not load Recipes view", String.valueOf(ex.getMessage()));
        }
    }

    @FXML
    private void onAddMissingToShoppingList(ActionEvent e) {
        if (missingNamesLower.isEmpty()) return;
        for (String nameLower : missingNamesLower) {
            String display = nameLower; // Best effort; we only tracked lower.
            try {
                ShoppingListService.addItem(new ShoppingItem(display, 1));
            } catch (IllegalArgumentException ignored) {
                // Skip invalid names
            }
        }
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Shopping List");
        a.setHeaderText("Added missing ingredients");
        a.setContentText("Missing ingredients were added with quantity 1.");
        a.showAndWait();
    }
}


