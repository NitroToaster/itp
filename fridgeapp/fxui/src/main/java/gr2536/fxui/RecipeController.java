package gr2536.fxui;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

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

    private final RecipeService svc = new RecipeService();

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
        setLoading(true);
        Task<List<RecipeModels.RecipeCard>> task = new Task<>() {
            @Override protected List<RecipeModels.RecipeCard> call() {
                return svc.search(query);
            }
        };
        task.setOnSucceeded(ev -> {
            List<RecipeModels.RecipeCard> cards = task.getValue();
            recipeContainer.getChildren().clear();
            if (cards != null) {
                for (RecipeModels.RecipeCard c : cards) {
                    addRecipeCard(c);
                }
            }
            if (noResultsLabel != null) {
                boolean none = cards == null || cards.isEmpty();
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
            setLoading(false);
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            showError("Failed to load recipes", ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
            if (noResultsLabel != null) {
                noResultsLabel.setText("Failed to load recipes.");
                noResultsLabel.setVisible(true);
                noResultsLabel.setManaged(true);
            }
            setLoading(false);
        });
        new Thread(task, "recipes-load").start();
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
        Task<RecipeModels.RecipeMeta> task = new Task<>() {
            @Override protected RecipeModels.RecipeMeta call() {
                return svc.meta();
            }
        };
        task.setOnSucceeded(ev -> {
            var meta = task.getValue();
            if (meta == null) return;
            if (categoryBox != null) categoryBox.getItems().setAll(meta.categories());
            if (areaBox != null) areaBox.getItems().setAll(meta.areas());
        });
        // Errors are non-fatal for meta; ignore
        new Thread(task, "recipes-meta").start();
    }

    @FXML
    private void onApplyFilters(ActionEvent e) {
        setLoading(true);
        List<String> chips = parseChips();
        boolean matchAll = matchModeBox == null || !"Any".equals(matchModeBox.getSelectionModel().getSelectedItem());
        int minMatched = minMatchedSpinner != null && minMatchedSpinner.getValue() != null ? minMatchedSpinner.getValue() : 1;
        String category = categoryBox != null ? categoryBox.getSelectionModel().getSelectedItem() : null;
        String area = areaBox != null ? areaBox.getSelectionModel().getSelectedItem() : null;
        String name = (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) ? searchField.getText().trim() : null;

        Task<List<RecipeModels.RecipeCardMatch>> task = new Task<>() {
            @Override protected List<RecipeModels.RecipeCardMatch> call() {
                return svc.filter2(chips, matchAll, minMatched, category, area, name, 30, 0);
            }
        };
        task.setOnSucceeded(ev -> {
            var matches = task.getValue();
            recipeContainer.getChildren().clear();
            if (matches != null) {
                for (var m : matches) addRecipeCardWithBadge(m);
            }
            if (noResultsLabel != null) {
                boolean none = matches == null || matches.isEmpty();
                noResultsLabel.setText(none ? "No recipes match your filters." : "");
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
            setLoading(false);
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            showError("Failed to apply filters", ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
            setLoading(false);
        });
        new Thread(task, "recipes-apply-filters").start();
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
        setLoading(true);
        var items = FridgeService.getFridge().listItems();
        
        // Also grab meta filters from the UI
        String category = categoryBox != null ? categoryBox.getSelectionModel().getSelectedItem() : null;
        String area = areaBox != null ? areaBox.getSelectionModel().getSelectedItem() : null;
        String name = (searchField != null && searchField.getText() != null && !searchField.getText().isBlank()) ? searchField.getText().trim() : null;

        if (items.isEmpty()) {
            if (noResultsLabel != null) {
                noResultsLabel.setText("Your fridge is empty.");
                noResultsLabel.setVisible(true);
                noResultsLabel.setManaged(true);
            }
            recipeContainer.getChildren().clear();
            setLoading(false);
            return;
        }
        Task<List<RecipeModels.RecipeCardMatch>> task = new Task<>() {
            @Override protected List<RecipeModels.RecipeCardMatch> call() throws Exception {
                // 1. Sync local fridge with backend
                svc.replaceInventory(items);
                // 2. Call filter2 with NO ingredients (so backend uses its own, now-synced fridge)
                //    but pass along the other filters.
                return svc.filter2(List.of(), false, 1, category, area, name, 5, 0);
            }
        };
        task.setOnSucceeded(ev -> {
            var matches = task.getValue();
            recipeContainer.getChildren().clear();
            if (matches != null) for (var m : matches) addRecipeCardWithBadge(m);
            if (noResultsLabel != null) {
                boolean none = matches == null || matches.isEmpty();
                noResultsLabel.setText(none ? "No recipes match your fridge." : "");
                noResultsLabel.setVisible(none);
                noResultsLabel.setManaged(none);
            }
            setLoading(false);
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            showError("Failed to filter by fridge ingredients", ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
            setLoading(false);
        });
        new Thread(task, "recipes-filter-fridge").start();
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
            UiUtil.showError("Navigation Error", "Could not open recipe details", String.valueOf(ex.getMessage()));
        }
    }

    private void showError(String header, Exception exception) {
        UiUtil.showError("Error", header, exception.getMessage());
    }

    @FXML
    private void onNavigateToFridge(ActionEvent e) {
        try {
            UiUtil.switchScene(root, "/gr2536/fxui/FridgeApp.fxml", "Fridge App");
        } catch (Exception ex) {
            UiUtil.showError("Navigation Error", "Could not load Fridge interface", String.valueOf(ex.getMessage()));
        }
    }
}


