package gr2536.fxui;

import java.util.Objects;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class RecipeController {

    @FXML
    private AnchorPane root;

    @FXML
    private FlowPane recipeContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label noResultsLabel;

    @FXML
    private void initialize() {
        // Initial load
        loadRecipes("chicken");
    }

    @FXML
    private void onSearch(ActionEvent e) {
        String q = searchField != null && searchField.getText() != null ? searchField.getText().trim() : "";
        loadRecipes(q.isEmpty() ? "chicken" : q);
    }

    private void loadRecipes(String query) {
        try {
            RecipeService svc = new RecipeService();
            List<RecipeModels.RecipeCard> cards = svc.search(query);
            recipeContainer.getChildren().clear();
            for (RecipeModels.RecipeCard c : cards) {
                addRecipeCard(c);
            }
            if (noResultsLabel != null) {
                boolean none = cards == null || cards.isEmpty();
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
        } catch (Exception e) {
            showError("Failed to load recipes", e);
            if (noResultsLabel != null) {
                noResultsLabel.setText("Failed to load recipes.");
                noResultsLabel.setVisible(true);
                noResultsLabel.setManaged(true);
            }
        }
    }

    private void addRecipeCard(RecipeModels.RecipeCard cardData) {
        Image image = new Image(cardData.image(), true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Label title = new Label(cardData.title());
        title.getStyleClass().add("recipe-title");
        title.setWrapText(true);

        VBox card = new VBox(8, imageView, title);
        card.setPadding(new Insets(8));
        card.getStyleClass().addAll("panel", "recipe-card");
        card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> showRecipeDetails(cardData.id()));

        recipeContainer.getChildren().add(card);
    }

    private void showRecipeDetails(String recipeId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/RecipeDetail.fxml"));
            Parent root2 = loader.load();
            RecipeDetailController ctrl = loader.getController();
            ctrl.setRecipeId(recipeId);
            ctrl.load();

            Scene scene = new Scene(root2);
            scene.getStylesheets().add(getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Recipe Details");
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Navigation Error");
            a.setHeaderText("Could not open recipe details");
            a.setContentText(String.valueOf(ex.getMessage()));
            a.showAndWait();
        }
    }

    private void showError(String header, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    @FXML
    private void onNavigateToFridge(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/FridgeApp.fxml"));
            Parent fridgeRoot = loader.load();

            Scene fridgeScene = new Scene(fridgeRoot);
            fridgeScene.getStylesheets().add(getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm());

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(fridgeScene);
            stage.setTitle("Fridge App");

        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Navigation Error");
            a.setHeaderText("Could not load Fridge interface");
            a.setContentText(String.valueOf(ex.getMessage()));
            a.showAndWait();
        }
    }
}


