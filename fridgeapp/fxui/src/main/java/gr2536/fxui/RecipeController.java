package gr2536.fxui;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    @FXML private TextField ingredientsField;
    @FXML private javafx.scene.control.ComboBox<String> matchModeBox;
    @FXML private javafx.scene.control.Spinner<Integer> minMatchedSpinner;
    @FXML private javafx.scene.control.ComboBox<String> categoryBox;
    @FXML private javafx.scene.control.ComboBox<String> areaBox;
    @FXML private javafx.scene.control.ProgressIndicator loadingIndicator;

    @FXML
    private void initialize() {
        // Initial load
        loadRecipes("chicken");
        setupFilters();
        loadMeta();
    }

    @FXML
    private void onSearch(ActionEvent e) {
        String q = searchField != null && searchField.getText() != null ? searchField.getText().trim() : "";
        loadRecipes(q.isEmpty() ? "chicken" : q);
    }

    private void loadRecipes(String query) {
        try {
            setLoading(true);
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
        } finally {
            setLoading(false);
        }
    }

    private void setupFilters() {
        if (matchModeBox != null) {
            matchModeBox.getItems().setAll("All", "Any");
            matchModeBox.getSelectionModel().select("All");
        }
        if (minMatchedSpinner != null) {
            minMatchedSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        }
    }

    private void loadMeta() {
        try {
            RecipeService svc = new RecipeService();
            var meta = svc.meta();
            if (categoryBox != null) {
                categoryBox.getItems().setAll(meta.categories());
            }
            if (areaBox != null) {
                areaBox.getItems().setAll(meta.areas());
            }
        } catch (Exception ex) {
            // Meta is optional; ignore errors and keep combos empty
        }
    }

    @FXML
    private void onApplyFilters(ActionEvent e) {
        try {
            setLoading(true);
            RecipeService svc = new RecipeService();
            List<String> chips = parseChips();
            boolean matchAll = matchModeBox == null || !"Any".equals(matchModeBox.getSelectionModel().getSelectedItem());
            int minMatched = minMatchedSpinner != null && minMatchedSpinner.getValue() != null ? minMatchedSpinner.getValue() : 1;
            String category = categoryBox != null ? categoryBox.getSelectionModel().getSelectedItem() : null;
            String area = areaBox != null ? areaBox.getSelectionModel().getSelectedItem() : null;
            String name = (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) ? searchField.getText().trim() : null;

            List<RecipeModels.RecipeCardMatch> matches = svc.filter2(chips, matchAll, minMatched, category, area, name, 30, 0);
            recipeContainer.getChildren().clear();
            for (var m : matches) {
                addRecipeCardWithBadge(m);
            }
            if (noResultsLabel != null) {
                boolean none = matches == null || matches.isEmpty();
                noResultsLabel.setText(none ? "No recipes match your filters." : "");
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
        } catch (Exception ex) {
            showError("Failed to apply filters", (Exception) ex);
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void onClearFilters(ActionEvent e) {
        if (ingredientsField != null) ingredientsField.clear();
        if (matchModeBox != null) matchModeBox.getSelectionModel().select("All");
        if (minMatchedSpinner != null) minMatchedSpinner.getValueFactory().setValue(1);
        if (categoryBox != null) categoryBox.getSelectionModel().clearSelection();
        if (areaBox != null) areaBox.getSelectionModel().clearSelection();
        recipeContainer.getChildren().clear();
        loadRecipes("chicken");
    }

    private List<String> parseChips() {
        List<String> chips = new ArrayList<>();
        if (ingredientsField != null && ingredientsField.getText() != null) {
            for (String part : ingredientsField.getText().split(",")) {
                String s = part.trim();
                if (!s.isBlank()) chips.add(s);
            }
        }
        return chips;
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
            loadingIndicator.setManaged(loading);
        }
    }

    @FXML
    private void onFilterByFridge(ActionEvent e) {
        try {
            setLoading(true);
            var items = FridgeService.getFridge().listItems();
            List<String> names = items.stream()
                .map(gr2536.core.Item::getName)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .collect(Collectors.toList());
            if (names.isEmpty()) {
                if (noResultsLabel != null) {
                    noResultsLabel.setText("Your fridge is empty.");
                    noResultsLabel.setVisible(true);
                    noResultsLabel.setManaged(true);
                }
                recipeContainer.getChildren().clear();
                return;
            }
            RecipeService svc = new RecipeService();
            // matchAll = false so ANY ingredient qualifies; minMatched = 1 ensures at least one match.
            List<RecipeModels.RecipeCardMatch> matches = svc.filter2(names, false, 1, null, null, null, 50, 0);
            recipeContainer.getChildren().clear();
            for (var m : matches) {
                addRecipeCardWithBadge(m);
            }
            if (noResultsLabel != null) {
                boolean none = matches == null || matches.isEmpty();
                noResultsLabel.setText(none ? "No recipes match your fridge." : "");
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
        } catch (Exception ex) {
            showError("Failed to filter by fridge ingredients", (Exception) ex);
        } finally {
            setLoading(false);
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

    private void addRecipeCardWithBadge(RecipeModels.RecipeCardMatch cardData) {
        Image image = new Image(cardData.image(), true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Label title = new Label(cardData.title() + " (" + cardData.matched() + "/" + cardData.total() + ")");
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


