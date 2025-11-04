package gr2536.fxui;

import javafx.event.ActionEvent;
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
    private String recipeId;
    private Set<String> missingNamesLower = new HashSet<>();

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void load() {
        try {
            var d = service.details(recipeId);
            titleLabel.setText(d.title());
            if (d.image() != null && !d.image().isBlank()) {
                imageView.setImage(new Image(d.image(), true));
            }
            metaLabel.setText(String.format("%s • %s", nullSafe(d.category()), nullSafe(d.area())));
            // Compare with fridge
            var fridge = FridgeService.getFridge();
            missingNamesLower.clear();
            var items = d.ingredients().stream().map(i -> {
                String name = i.name() == null ? "" : i.name().trim();
                boolean have = fridge.getQuantity(name) > 0;
                if (!have && !name.isBlank()) missingNamesLower.add(name.toLowerCase());
                return name + (i.measure() == null || i.measure().isBlank() ? "" : " - " + i.measure());
            }).toList();
            ingredientsList.getItems().setAll(items);

            // Cell styling for have/missing
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
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setHeaderText("Failed to load recipe");
            a.setContentText(String.valueOf(e.getMessage()));
            a.showAndWait();
        }
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    @FXML
    private void onBack(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/Recipe.fxml"));
            Parent recipeRoot = loader.load();
            Scene scene = new Scene(recipeRoot);
            scene.getStylesheets().add(getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Recipes");
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Navigation Error");
            a.setHeaderText("Could not load Recipes view");
            a.setContentText(String.valueOf(ex.getMessage()));
            a.showAndWait();
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


