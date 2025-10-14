/* Used Github CoPilot to refactor the initialize method into several private methods */

package gr2536.fxui;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import gr2536.core.Fridge;
import gr2536.core.Item;
import gr2536.core.NameMatchMode;
import gr2536.core.SearchCriteria;
import gr2536.core.SearchSort;
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
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import gr2536.core.FridgeFileManager;
import gr2536.utils.FridgeJsonFileManager;

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

    /** Root pane/Background. */
    @FXML
    private AnchorPane root;

    // ========== Search controls ==========
    /** Search text field for filtering by name. */
    @FXML
    private TextField searchField;
    /** Button to execute search. */
    @FXML
    private Button searchButton;
    /** Button to clear search and show all items. */
    @FXML
    private Button clearSearchButton;
    
    // ========== List header columns ==========
    /** Column header for Name with sort arrow. */
    @FXML
    private Label nameHeaderLabel;
    /** Column header for Quantity with sort arrow. */
    @FXML
    private Label quantityHeaderLabel;
    /** Column header for Expiration with sort arrow. */
    @FXML
    private Label expirationHeaderLabel;

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
    /** Button to navigate to shopping list. */
    @FXML
    private Button shoppingListButton;

    /** Sidebar button to reset the view to fridge overview. */
    @FXML
    private Button navFridgeButton;

    /** Button to toggle sidebar visibility. */
    @FXML
    private Button toggleSidebarButton;

    /** Button to show sidebar when collapsed (floating button). */
    @FXML
    private Button showSidebarButton;

    /** Collapsible filters panel container. */
    @FXML
    private javafx.scene.layout.VBox filtersPanel;

    /** Toggle button for filters in header. */
    @FXML
    private Button toggleFiltersButton;

    /** Sidebar container node for toggling. */
    @FXML
    private javafx.scene.layout.VBox sidebar;

    /** List view displaying items in the fridge. */
    @FXML
    private ListView<Item> fridgeList;
    /** ListView backing data. */
    private final ObservableList<Item> items = FXCollections.observableArrayList();

    // Persistence
    private FridgeFileManager ffm;
    private final String filename = "fridge.json";
    private Fridge fridge;

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
     * Current sort order for the list.
     */
    private SearchSort currentSort = SearchSort.DEFAULT;

    /**
     * Initializes the UI: delegates setup to helper methods for clarity and
     * maintainability.
     * Called by the FXMLLoader after FXML fields are injected.
     */
    @FXML
    private void initialize() {
        setupFridge();

        setupListView();
        setupSearchControls();
        setupFilterControls();
        setupButtonEvents();
        setupBackgroundClickHandling();
        setupListViewSelectionClearing();
        setupLiveSearch();
        refreshFromModel();
    }

    /** Enables debounced live search on searchField text changes. */
    private void setupLiveSearch() {
        if (searchField == null) return;
        PauseTransition debounce = new PauseTransition(Duration.millis(250));
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            debounce.stop();
            debounce.setOnFinished(e -> onSearch(new ActionEvent()));
            debounce.playFromStart();
        });
    }

    /** Initializes the Fridge domain model and file manager. */
    private void setupFridge() {
        ffm = new FridgeJsonFileManager();
        fridge = ffm.readFridgeData(filename);  
        fridge.setFileManager(ffm, filename);
        FridgeService.setFridge(fridge);
    }

    /** Sets up the ListView cell factory and selection toggling. */
    private void setupListView() {
        fridgeList.setItems(items);
        fridgeList.setCellFactory(lv -> createItemCell());
    }

    /**
     * Creates a custom {@link ListCell} that shows item details and contextual actions.
     * <p>Buttons (+/–/Del) appear on hover or selection and delegate to the
     * controller’s handlers while keeping the row selection consistent.
     * </p>
     * @return a cell factory for {@link Item} rows
     */
    private ListCell<Item> createItemCell() {
    return new ListCell<>() {

        private final Label title = new Label();
        private final Button plus = new Button("+");
        private final Button minus = new Button("-");
        private final Button del = new Button("Del");
        private final HBox actions = new HBox(6, plus, minus, del);
        private final Region spacer = new Region();
        private final HBox root = new HBox(10, title, spacer, actions);

        {
            // CSS
            plus.getStyleClass().addAll("cell-action", "cell-plus");
            minus.getStyleClass().addAll("cell-action", "cell-minus");
            del.getStyleClass().addAll("cell-action", "cell-danger");

            // Layout
            spacer.setMinWidth(0);
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // Only show the action buttons when selected or hovered
            actions.setManaged(true);
            actions.setVisible(true);
            
            var show = selectedProperty().or(hoverProperty());
            actions.opacityProperty().bind(
                Bindings.when(show).then(1.0).otherwise(0.0)
            );

            actions.mouseTransparentProperty().bind(show.not());

            addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,
                e -> FridgeAppController.this.handleCellToggle(this, e));

            // Wire to existing controller methods:
            plus.setOnAction(e -> {
                if (getItem() == null) return;
                // 1) make this row the selection
                fridgeList.getSelectionModel().select(getIndex());
                // 2) call the controller's existing handler
                onAddOne(new javafx.event.ActionEvent(plus, plus));
                e.consume();
            });

            minus.setOnAction(e -> {
                if (getItem() == null) return;
                fridgeList.getSelectionModel().select(getIndex());
                onRemoveOne(new javafx.event.ActionEvent(minus, minus));
                e.consume();
            });

            del.setOnAction(e -> {
                if (getItem() == null) return;
                fridgeList.getSelectionModel().select(getIndex());
                onRemoveAll(new javafx.event.ActionEvent(del, del));
                e.consume();
            });

            //Tooltips
            plus.setTooltip(new javafx.scene.control.Tooltip("Add one"));
            minus.setTooltip(new javafx.scene.control.Tooltip("Remove one"));
            del.setTooltip(new javafx.scene.control.Tooltip("Remove all"));
        }

        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String date = item.getExpirationDate() == null ? "(no date)" : item.getExpirationDate().toString();
                title.setText(item.getName() + " - " + item.getQuantity() + " - exp: " + date);
                setText(null);
                setGraphic(root);
            }
        }
    };
    }

    /** Handles toggling selection when clicking an already-selected cell,
     * unless the click originated inside the control
     * */
    private void handleCellToggle(ListCell<Item> cell, javafx.scene.input.MouseEvent e) {
        if (cell.isEmpty()) return;

        javafx.scene.Node target = (javafx.scene.Node) e.getTarget();
        if (isInsideControl(target)) {
            return;
        }

        int index = cell.getIndex();
        if (fridgeList.getSelectionModel().getSelectedIndex() == index) {
            fridgeList.getSelectionModel().clearSelection();
            e.consume();
        }
    }


    /**
     * Sets up search-related controls and button bindings.
     */
    private void setupSearchControls() {
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
        
        // Initialize list header sort indicators
        updateSortIndicators();
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
        if (shoppingListButton != null) {
            shoppingListButton.setOnAction(this::onNavigateToShoppingList);
        }
        if (navFridgeButton != null) {
            navFridgeButton.setOnAction(this::onNavigateToFridge);
        }
        if (toggleSidebarButton != null) {
            toggleSidebarButton.setOnAction(this::onToggleSidebar);
            toggleSidebarButton.getStyleClass().removeAll("collapsed", "expanded");
            toggleSidebarButton.getStyleClass().add("expanded");
        }
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

            // Execute search using all match modes and update view
            List<Item> results = getFridge().searchWithAllModes(
                query, minQty, maxQty, fromDate, untilDate, includeUnknown, currentSort
            );
            items.setAll(results);
            isFiltered = true;
            
            // Store search criteria for refreshFromModel
            currentSearchCriteria = new SearchCriteria(
                query, NameMatchMode.CONTAINS, minQty, maxQty, fromDate, untilDate, includeUnknown, currentSort
            );

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

        // Reset filter fields
        filterMinQuantitySpinner.getValueFactory().setValue(0);
        filterMaxQuantitySpinner.getValueFactory().setValue(99);
        filterExpirationFromPicker.setValue(null);
        filterExpirationUntilPicker.setValue(null);
        filterIncludeUnknownCheck.setSelected(true);

        // Reset sort to default
        currentSort = SearchSort.DEFAULT;
        updateSortIndicators();

        // Clear criteria and refresh
        currentSearchCriteria = null;
        isFiltered = false;
        refreshFromModel();
    }

    
    /**
     * Load items from a .json file.
     * 
     * @param e ActionEvent from apply filter button
     */
    @FXML
    private void onLoad(ActionEvent e) {
    Window window = fridgeList.getScene().getWindow();
    FileChooser fc = new FileChooser();
    fc.setTitle("Open fridge file");
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
    File f = fc.showOpenDialog(window);

    if (f != null) {
        try {
            Fridge newFridge = ffm.readFridgeData(f.getAbsolutePath());

            newFridge.setFileManager(ffm, f.getAbsolutePath());

            fridge = newFridge;
            FridgeService.setFridge(fridge);

            refreshFromModel();

        } catch (Exception exception) {
            showError("Could not read file.", exception);
        }
    }
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

            // Execute filter using all match modes and update view
            List<Item> results = getFridge().searchWithAllModes(
                searchQuery, minQty, maxQty, fromDate, untilDate, includeUnknown, currentSort
            );
            items.setAll(results);
            isFiltered = true;
            
            // Store search criteria for refreshFromModel
            currentSearchCriteria = new SearchCriteria(
                searchQuery, NameMatchMode.CONTAINS, minQty, maxQty, fromDate, untilDate, includeUnknown, currentSort
            );

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
     * Navigate back to the fridge overview, clearing filters if necessary.
     * @param e action event from sidebar button
     */
    @FXML
    private void onNavigateToFridge(ActionEvent e) {
        if (isFiltered || currentSearchCriteria != null) {
            onClearSearch(e);
        } else {
            refreshFromModel();
        }
    }

    /**
     * Toggles the sidebar visibility, adjusting layout spacing and toggle text.
     * @param e action event from the toggle button
     */
    @FXML
    private void onToggleSidebar(ActionEvent e) {
        if (sidebar == null || toggleSidebarButton == null) {
            return;
        }

        boolean currentlyVisible = sidebar.isManaged() && sidebar.isVisible();
        sidebar.setManaged(!currentlyVisible);
        sidebar.setVisible(!currentlyVisible);

        // Update sidebar toggle button (chevron in sidebar)
        toggleSidebarButton.setText(currentlyVisible ? "›" : "‹");
        toggleSidebarButton.getStyleClass().removeAll("expanded", "collapsed");
        toggleSidebarButton.getStyleClass().add(currentlyVisible ? "collapsed" : "expanded");

        // Show/hide the hamburger menu button in workspace
        if (showSidebarButton != null) {
            showSidebarButton.setVisible(currentlyVisible);
            showSidebarButton.setManaged(currentlyVisible);
        }

        // Do not toggle root style classes; keep global look stable
    }

    /**
     * Toggles the filters panel visibility from the header.
     * Keeps layout tidy by binding both managed and visible together.
     * @param e action event from the Filters button
     */
    @FXML
    private void onToggleFilters(ActionEvent e) {
        if (filtersPanel == null) {
            return;
        }
        boolean isVisible = filtersPanel.isVisible();
        filtersPanel.setVisible(!isVisible);
        filtersPanel.setManaged(!isVisible);
        if (toggleFiltersButton != null) {
            toggleFiltersButton.setText(isVisible ? "Filters" : "Hide Filters");
        }
    }

    /**
     * Shows the Add Item dialog.
     *
     * @return the created {@link Item}, or {@code null} if the dialog was cancelled
    */
    private Item showAddItemDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gr2536/fxui/AddItemDialog.fxml"));
            DialogPane pane = loader.load();
            AddItemDialogController ctrl = loader.getController();

            Dialog<Item> dialog = new Dialog<>();
            dialog.setTitle("Add Item");
            dialog.setDialogPane(pane);
            dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/gr2536/fxui/FridgeApp.css").toExternalForm()
            );

            dialog.setResultConverter(bt ->
                bt != null && (bt == ButtonType.OK || bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                    ? ctrl.buildItemOrThrow()
                    : null
            );
            return dialog.showAndWait().orElse(null);        

        } catch (Exception ex) {
            showException("Could not open Add Item dialog", ex);
            return null;
        }
    }

    /**
     * Opens the Add Item dialog, and adds the resulting item to the fridge.
     * Afterwards refreshes the list (respecting any active filters).
     * 
     * @param e from the Add Item button
     * @see AddItemDialogController
     */
    @FXML
    private void onAdd(ActionEvent e) {
        Item item = showAddItemDialog();
        if (item == null) {
            return;
        }
        getFridge().add(item);
        refreshFromModel();
    }

    /**
     * Increments the selected item by exactly one and reselects the same bucket.
     *
     * @param e Action Event from the plus (+) button
     */
    private void onAddOne(ActionEvent e) {
        Item selected = fridgeList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        //Increment by 1
        getFridge().add(new Item(selected.getName(), 1, selected.getExpirationDate()));

        // Refresh view (respecting current filters if active)
        Item prev = selected;
        refreshFromModel();
        reselectSameBucket(prev);
    }

    /**
     * Decrements the selected item by exactly one (FIFO by expiration date)
     * and reselects the same bucket.
     *
     * @param e Action Event from the minus (-) button
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
        Item prev = selected;
        refreshFromModel();
        reselectSameBucket(prev);
    }

    /**
     * Removes all units of the selected item from the fridge, and refreshes the list
     * 
     * @param e Action Event from the delete (del) button
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
            showException("Navigation Error", new RuntimeException("Could not load Shopping List interface: " + exception.getMessage()));
        }
    }

    /**
     * Refreshes ListView with items from the fridge.
     * If search/filter is active, re-applies the current criteria with updated sort.
     * Otherwise, shows all items with current sort order applied.
     */
    private void refreshFromModel() {
        if (isFiltered && currentSearchCriteria != null) {
            // Re-apply current search/filter criteria with updated sort using all match modes
            items.setAll(getFridge().searchWithAllModes(
                currentSearchCriteria.nameQuery(),
                currentSearchCriteria.minQuantity(),
                currentSearchCriteria.maxQuantity(),
                currentSearchCriteria.expirationFrom(),
                currentSearchCriteria.expirationUntil(),
                currentSearchCriteria.includeUnknownExpiration(),
                currentSort  // Use current sort instead of the one in criteria
            ));
        } else if (currentSort != SearchSort.DEFAULT) {
            // Apply sort to all items without other filters
            items.setAll(getFridge().searchWithAllModes(
                null, null, null, null, null, true, currentSort
            ));
        } else {
            // Show all items in default order
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
     * Show an error alert with the given header and exception message.
     * Made due to error message in showError not being fully shown, due to being it truncated.
     * 
     * @param header the error header text
     * @param exception the exception containing the error message
     */
    private void showException(String header, Throwable ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(String.valueOf(ex.getMessage()));

        var sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        var ta = new javafx.scene.control.TextArea(sw.toString());
        ta.setEditable(false); ta.setWrapText(false);
        ta.setMaxWidth(Double.MAX_VALUE); ta.setMaxHeight(Double.MAX_VALUE);

        var gp = new javafx.scene.layout.GridPane();
        gp.setMaxWidth(Double.MAX_VALUE);
        gp.add(ta, 0, 0);

        a.getDialogPane().setExpandableContent(gp);
        a.showAndWait();
    }

    /**
     * Gets the shared Fridge instance from FridgeService.
     * 
     * @return the current Fridge instance
     */
    private Fridge getFridge() {
        return FridgeService.getFridge();
    }

    /**
     * Reselects and scrolls to the item cell that matches the previous selection
     * by case-insensitive name and expiration date.
     *
     * @param prev the previously selected item (name/date used for matching)
     */
    private void reselectSameBucket(Item prev) {
    for (int i = 0; i < items.size(); i++) {
        Item it = items.get(i);
        boolean sameName = it.getName().equalsIgnoreCase(prev.getName());
        boolean sameDate = java.util.Objects.equals(it.getExpirationDate(), prev.getExpirationDate());
        if (sameName && sameDate) {
            fridgeList.getSelectionModel().select(i);
            fridgeList.scrollTo(i);
            break;
        }
    }
    }
    
    // ========== Sort Handlers ==========
    
    /**
     * Handles clicking on the Name column header to cycle through sort modes.
     * Cycles: DEFAULT → NAME_ASC → NAME_DESC → DEFAULT
     *
     * @param e MouseEvent from the name header label
     */
    @FXML
    private void onSortByName(javafx.scene.input.MouseEvent e) {
        if (currentSort == SearchSort.DEFAULT || currentSort == SearchSort.EXPIRATION_ASC 
            || currentSort == SearchSort.EXPIRATION_DESC || currentSort == SearchSort.QUANTITY_DESC) {
            currentSort = SearchSort.NAME_ASC;
        } else if (currentSort == SearchSort.NAME_ASC) {
            currentSort = SearchSort.NAME_DESC;
        } else {
            currentSort = SearchSort.DEFAULT;
        }
        updateSortIndicators();
        refreshFromModel();
    }
    
    /**
     * Handles clicking on the Quantity column header to cycle through sort modes.
     * Cycles: DEFAULT → QUANTITY_ASC → QUANTITY_DESC → DEFAULT
     *
     * @param e MouseEvent from the quantity header label
     */
    @FXML
    private void onSortByQuantity(javafx.scene.input.MouseEvent e) {
        if (currentSort == SearchSort.DEFAULT || currentSort == SearchSort.NAME_ASC 
            || currentSort == SearchSort.NAME_DESC || currentSort == SearchSort.EXPIRATION_ASC 
            || currentSort == SearchSort.EXPIRATION_DESC) {
            currentSort = SearchSort.QUANTITY_ASC;
        } else if (currentSort == SearchSort.QUANTITY_ASC) {
            currentSort = SearchSort.QUANTITY_DESC;
        } else {
            currentSort = SearchSort.DEFAULT;
        }
        updateSortIndicators();
        refreshFromModel();
    }
    
    /**
     * Handles clicking on the Expiration column header to cycle through sort modes.
     * Cycles: DEFAULT → EXPIRATION_ASC → EXPIRATION_DESC → DEFAULT
     *
     * @param e MouseEvent from the expiration header label
     */
    @FXML
    private void onSortByExpiration(javafx.scene.input.MouseEvent e) {
        if (currentSort == SearchSort.DEFAULT || currentSort == SearchSort.NAME_ASC 
            || currentSort == SearchSort.NAME_DESC || currentSort == SearchSort.QUANTITY_DESC) {
            currentSort = SearchSort.EXPIRATION_ASC;
        } else if (currentSort == SearchSort.EXPIRATION_ASC) {
            currentSort = SearchSort.EXPIRATION_DESC;
        } else {
            currentSort = SearchSort.DEFAULT;
        }
        updateSortIndicators();
        refreshFromModel();
    }
    
    /**
     * Updates the visual indicators (arrows) on column headers based on current sort.
     * Adds ↑ for ascending, ↓ for descending, and highlights the active column.
     */
    private void updateSortIndicators() {
        // Reset all headers
        nameHeaderLabel.getStyleClass().remove("sorted");
        quantityHeaderLabel.getStyleClass().remove("sorted");
        expirationHeaderLabel.getStyleClass().remove("sorted");
        
        // Set text based on current sort
        switch (currentSort) {
            case NAME_ASC:
                nameHeaderLabel.setText("Name ↑");
                nameHeaderLabel.getStyleClass().add("sorted");
                quantityHeaderLabel.setText("Quantity");
                expirationHeaderLabel.setText("Expiration");
                break;
            case NAME_DESC:
                nameHeaderLabel.setText("Name ↓");
                nameHeaderLabel.getStyleClass().add("sorted");
                quantityHeaderLabel.setText("Quantity");
                expirationHeaderLabel.setText("Expiration");
                break;
            case QUANTITY_ASC:
                nameHeaderLabel.setText("Name");
                quantityHeaderLabel.setText("Quantity ↑");
                quantityHeaderLabel.getStyleClass().add("sorted");
                expirationHeaderLabel.setText("Expiration");
                break;
            case QUANTITY_DESC:
                nameHeaderLabel.setText("Name");
                quantityHeaderLabel.setText("Quantity ↓");
                quantityHeaderLabel.getStyleClass().add("sorted");
                expirationHeaderLabel.setText("Expiration");
                break;
            case EXPIRATION_ASC:
                nameHeaderLabel.setText("Name");
                quantityHeaderLabel.setText("Quantity");
                expirationHeaderLabel.setText("Expiration ↑");
                expirationHeaderLabel.getStyleClass().add("sorted");
                break;
            case EXPIRATION_DESC:
                nameHeaderLabel.setText("Name");
                quantityHeaderLabel.setText("Quantity");
                expirationHeaderLabel.setText("Expiration ↓");
                expirationHeaderLabel.getStyleClass().add("sorted");
                break;
            default:
                nameHeaderLabel.setText("Name");
                quantityHeaderLabel.setText("Quantity");
                expirationHeaderLabel.setText("Expiration");
                break;
        }
    }
}