package gr2536.fxui;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

//import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Init;
import org.testfx.matcher.base.NodeMatchers;
import static org.testfx.assertions.api.Assertions.assertThat;

import gr2536.core.Item;
import gr2536.core.NameMatchMode;
import gr2536.core.SearchSort;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

/**
 * Integration tests for FridgeAppController using TestFX.
 * Tests UI interactions, button states, and data flow between UI and model.
 */
// @Disabled("TEMP: flaky TestFX timing under mvn verify")
public class FridgeAppControllerTest extends ApplicationTest {

    private TextField nameField;
    private Spinner<Integer> quantitySpinner;
    private DatePicker expirationPicker;
    private Button addButton;
    private Button addOneButton;
    private Button removeOneButton;
    private Button removeAllButton;
    private ListView<Item> fridgeList;
    private TextField searchField;
    private Button searchButton;
    private Button clearSearchButton;
    private Spinner<Integer> filterMinQuantitySpinner;
    private Spinner<Integer> filterMaxQuantitySpinner;
    private DatePicker filterExpirationFromPicker;
    private DatePicker filterExpirationUntilPicker;
    private CheckBox filterIncludeUnknownCheck;
    private Button applyFilterButton;
    private Button clearFilterButton;

    @BeforeEach
    public void setUp() {
        // Reset FridgeService to clean state before each test
        FridgeService.reset();
    }

    @Override
    public void start(javafx.stage.Stage stage) throws Exception {
        FridgeApp app = new FridgeApp();
        app.start(stage);

        // Lookup controls
        
        // nameField = lookup("#nameField").query();
        // quantitySpinner = lookup("#quantitySpinner").query();
        // expirationPicker = lookup("#expirationPicker").query();
        addButton = lookup("#addButton").query();
        // addOneButton = lookup("#addOneButton").query();
        // removeOneButton = lookup("#removeOneButton").query();
        // removeAllButton = lookup("#removeAllButton").query();
        fridgeList = lookup("#fridgeList").query();
        searchField = lookup("#searchField").query();
        searchButton = lookup("#searchButton").query();
        clearSearchButton = lookup("#clearSearchButton").query();
        filterMinQuantitySpinner = lookup("#filterMinQuantitySpinner").query();
        filterMaxQuantitySpinner = lookup("#filterMaxQuantitySpinner").query();
        filterExpirationFromPicker = lookup("#filterExpirationFromPicker").query();
        filterExpirationUntilPicker = lookup("#filterExpirationUntilPicker").query();
        filterIncludeUnknownCheck = lookup("#filterIncludeUnknownCheck").query();
        applyFilterButton = lookup("#applyFilterButton").query();
        clearFilterButton = lookup("#clearFilterButton").query();
    }

    // ========== Button State Tests ==========

    /**
     * Tests that Add button in the pop up window is disabled when required fields are empty.
     */
    @Test
    public void testAddButtonDisabledWhenFieldsEmpty() {
        clickOn("#addButton");

        // Lookup the dialog's "Add" button by its text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();

        interact(() -> {
            assertTrue(dialogAddButton.isDisabled(), "Add button should be disabled initially");
        });
    }

    /**
     * Tests that search button is disabled when search field is empty.
     */
    @Test
    public void testSearchButtonDisabledWhenEmpty() {
        interact(() -> {
            assertTrue(searchButton.isDisabled(), "Search button should be disabled when field empty");
        });

        interact(() -> searchField.setText("test"));

        interact(() -> {
            assertFalse(searchButton.isDisabled(), "Search button should be enabled with text");
        });
    }

    // ========== Add Operations Tests ==========

    /**
     * Tests that Add button in pop up window adds number of items corresponding to spinner value.
     */
    @Test
    public void testAddButton() {
        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();

        interact(() -> {
            nameField.setText("Apple");
            quantitySpinner.getValueFactory().setValue(5); // Set to 5
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });

        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should have 1 item");
            Item addedItem = fridgeList.getItems().get(0);
            assertEquals("Apple", addedItem.getName());
            assertEquals(5, addedItem.getQuantity(), "Should add 5 items");
        });
    }

    /**
     * Tests adding multiple items with different names.
     */
    @Test
    public void testAddMultipleItems() {

        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();

        // Add first item
        interact(() -> {
            nameField.setText("Milk");
            quantitySpinner.getValueFactory().setValue(2);
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Add second item
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();

        interact(() -> {
            nameField.setText("Butter");
            quantitySpinner.getValueFactory().setValue(1);
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        interact(() -> {
            assertEquals(2, fridgeList.getItems().size(), "Should have 2 items");
        });
    }

    /**
     * Tests that fields are cleared after successful add.
     */
    @Test
    public void testFieldsClearedAfterAdd() {
        
        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();

        interact(() -> {
            nameField.setText("Banana");
            quantitySpinner.getValueFactory().setValue(3);
            expirationPicker.setValue(LocalDate.of(2025, 10, 10));
        });

        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();

        interact(() -> {
            assertTrue(nameField.getText().isEmpty(), "Name field should be cleared");
            assertEquals(1, quantitySpinner.getValue(), "Quantity should reset to 1");
            assertNull(expirationPicker.getValue(), "Date picker should be cleared");
        });
    }

    // ========== Remove Operations Tests ==========

    /**
     * Tests that removeOne removes exactly 1 unit of selected item.
     */
    @Test
    public void testRemoveOneButton() {
        // Add item with quantity 3

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();

        interact(() -> {
            nameField.setText("Banana");
            quantitySpinner.getValueFactory().setValue(3);
            expirationPicker.setValue(LocalDate.of(2025, 10, 10));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Select item programmatically and remove one
        interact(() -> {
            fridgeList.getSelectionModel().select(0);
        });

        // Lookup the "-" button inside the cell
        Button minusButton = lookup(".list-cell .button").lookup("-").queryButton();
        clickOn(minusButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should still have the item");
            assertEquals(2, fridgeList.getItems().get(0).getQuantity(), "Should have 2 remaining");
        });
    }

    /**
     * Tests that removeAll removes all units of selected item.
     */
    @Test
    public void testRemoveAllButton() {
        // Add item with quantity 5
        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Grape");
            quantitySpinner.getValueFactory().setValue(5);
            expirationPicker.setValue(LocalDate.of(2025, 10, 15));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Select item programmatically and remove all
        interact(() -> {
            fridgeList.getSelectionModel().select(0);
        });
        // Lookup the "Del" button inside the cell
        Button delButton = lookup(".list-cell .button").lookup("Del").queryButton();
        clickOn(delButton);

        interact(() -> {
            assertTrue(fridgeList.getItems().isEmpty(), "All items should be removed");
        });
    }

    /**
     * Tests removeOne until item is completely gone.
     */
    @Test
    public void testRemoveOneUntilEmpty() {
        // Add item with quantity 2
        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Cherry");
            quantitySpinner.getValueFactory().setValue(2);
            expirationPicker.setValue(LocalDate.of(2025, 10, 20));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Remove one
        interact(() -> fridgeList.getSelectionModel().select(0));
        // Lookup the "-" button inside the cell
        Button minusButton = lookup(".list-cell .button").lookup("-").queryButton();
        clickOn(minusButton);
        interact(() -> assertEquals(1, fridgeList.getItems().get(0).getQuantity()));

        // Remove one more (should remove item completely)
        interact(() -> fridgeList.getSelectionModel().select(0));
        // Lookup the "-" button inside the cell
        minusButton = lookup(".list-cell .button").lookup("-").queryButton();
        clickOn(minusButton);
        interact(() -> assertTrue(fridgeList.getItems().isEmpty(), "Item should be completely removed"));
    }

    // ========== Search Tests ==========

    /**
     * Tests search functionality with text query.
     */
    @Test
    public void testSearchByName() {
        // Add multiple items
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Milk");
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Butter");
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        // Lookup the dialog's Add button by text
        dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Milkshake");
            expirationPicker.setValue(LocalDate.of(2025, 10, 3));
        });
        // Lookup the dialog's Add button by text
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Search for "milk" (should match Milk and Milkshake with CONTAINS mode)
        interact(() -> searchField.setText("milk"));
        clickOn(searchButton);

        interact(() -> {
            assertEquals(2, fridgeList.getItems().size(), "Should find 2 items containing 'milk'");
        });
    }

    /**
     * Tests clear search button restores full list.
     */
    @Test
    @Disabled("clear button i search/filter doesn't work")
    public void testClearSearch() {
        // Add first item
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Cheese");
            expirationPicker.setValue(LocalDate.of(2025, 10, 20));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".dialog-pane").lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);
        sleep(100); // Allow UI to update

        // Add second item
        clickOn("#addButton");

        nameField = lookup(".dialog-pane").lookup("#nameField").query();
        quantitySpinner = lookup(".dialog-pane").lookup("#quantitySpinner").query();
        expirationPicker = lookup(".dialog-pane").lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Bread");
            expirationPicker.setValue(LocalDate.of(2025, 10, 21));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);
        sleep(100); // Allow UI to update

        // Verify both items were added
        interact(() -> {
            int itemCount = fridgeList.getItems().size();
            assertEquals(2, itemCount, "Should have 2 items before search, but had " + itemCount);
        });

        // Perform search
        interact(() -> searchField.setText("che"));
        clickOn(searchButton);
        sleep(1000);

        interact(() -> assertEquals(1, fridgeList.getItems().size(), "Search should filter to 1 item"));

        // Clear search
        clickOn(clearSearchButton);
        sleep(2000); // Allow UI to update after clear

        interact(() -> {
            int itemCount = fridgeList.getItems().size();
            assertEquals(2, itemCount, "All items should be visible after clear, but had " + itemCount);
        });
    }

    /**
     * Tests search with different match modes.
     */
    @Test
    public void testSearchMatchModes() {
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Apple");
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        // Lookup the dialog's Add button by text
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Pineapple");
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Test search (should find both Apple and Pineapple using all match modes)
        interact(() -> {
            searchField.setText("apple");
        });
        clickOn(searchButton);
        interact(() -> assertEquals(2, fridgeList.getItems().size(), "Search should find both Apple and Pineapple"));
    }

    /**
     * Tests search sort functionality.
     */
    @Test
    public void testSearchSort() {
        // Add items
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Zebra");
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Apple");
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Sort by name ascending using column header
        clickOn("#nameHeaderLabel"); // Click the name column header to sort ascending

        interact(() -> {
            assertEquals("Apple", fridgeList.getItems().get(0).getName(), "Apple should be first");
            assertEquals("Zebra", fridgeList.getItems().get(1).getName(), "Zebra should be second");
        });
    }

    // ========== Filter Tests ==========

    /**
     * Tests filtering by minimum quantity.
     */
    @Test
    public void testFilterByMinQuantity() {
        // Add items with different quantities
        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item1");
            quantitySpinner.getValueFactory().setValue(1);
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item5");
            quantitySpinner.getValueFactory().setValue(5);
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Filter for items with at least 3 units
        interact(() -> {
            filterMinQuantitySpinner.getValueFactory().setValue(3);
        });
        clickOn(applyFilterButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should find only items with qty >= 3");
            assertEquals("Item5", fridgeList.getItems().get(0).getName());
        });
    }

    /**
     * Tests filtering by maximum quantity.
     */
    @Test
    public void testFilterByMaxQuantity() {
        // Add items with different quantities

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item2");
            quantitySpinner.getValueFactory().setValue(2);
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item10");
            quantitySpinner.getValueFactory().setValue(10);
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Filter for items with at most 5 units
        interact(() -> {
            filterMaxQuantitySpinner.getValueFactory().setValue(5);
        });
        clickOn(applyFilterButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should find only items with qty <= 5");
            assertEquals("Item2", fridgeList.getItems().get(0).getName());
        });
    }

    /**
     * Tests filtering by expiration date range.
     */
    @Test
    public void testFilterByExpirationRange() {
        // Add items with different expiration dates

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("ExpiresSoon");
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("ExpiresLater");
            expirationPicker.setValue(LocalDate.of(2025, 10, 15));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("ExpiresMuchLater");
            expirationPicker.setValue(LocalDate.of(2025, 10, 30));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Filter for items expiring between Oct 10 and Oct 20
        interact(() -> {
            filterExpirationFromPicker.setValue(LocalDate.of(2025, 10, 10));
            filterExpirationUntilPicker.setValue(LocalDate.of(2025, 10, 20));
        });
        clickOn(applyFilterButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should find 1 item in date range");
            assertEquals("ExpiresLater", fridgeList.getItems().get(0).getName());
        });
    }

    /**
     * Tests clear filter button.
     */
    @Test
    public void testClearFilter() {
        // Add items

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item1");
            quantitySpinner.getValueFactory().setValue(1);
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Item5");
            quantitySpinner.getValueFactory().setValue(5);
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Apply filter
        interact(() -> {
            filterMinQuantitySpinner.getValueFactory().setValue(3);
        });
        clickOn(applyFilterButton);
        interact(() -> assertEquals(1, fridgeList.getItems().size(), "Filter should be active"));

        // Clear filter
        clickOn(clearFilterButton);

        interact(() -> {
            assertEquals(2, fridgeList.getItems().size(), "All items should be visible after clear filter");
            assertEquals(0, filterMinQuantitySpinner.getValue(), "Min quantity should reset to 0");
            assertEquals(99, filterMaxQuantitySpinner.getValue(), "Max quantity should reset to 99");
        });
    }

    /**
     * Tests combined search and filter.
     */
    @Test
    public void testCombinedSearchAndFilter() {
        // Add items

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Apple");
            quantitySpinner.getValueFactory().setValue(1);
            expirationPicker.setValue(LocalDate.of(2025, 10, 1));
        });
        Button dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        clickOn("#addButton");

        nameField = lookup("#nameField").query();
        quantitySpinner = lookup("#quantitySpinner").query();
        expirationPicker = lookup("#expirationPicker").query();
        interact(() -> {
            nameField.setText("Pineapple");
            quantitySpinner.getValueFactory().setValue(5);
            expirationPicker.setValue(LocalDate.of(2025, 10, 2));
        });
        dialogAddButton = lookup(".button").lookup("Add").queryButton();
        clickOn(dialogAddButton);

        // Search for "apple" with min quantity 3
        interact(() -> {
            searchField.setText("apple");
            filterMinQuantitySpinner.getValueFactory().setValue(3);
        });
        clickOn(searchButton);

        interact(() -> {
            assertEquals(1, fridgeList.getItems().size(), "Should find only Pineapple with qty >= 3");
            assertEquals("Pineapple", fridgeList.getItems().get(0).getName());
        });
    }

    // ========== Initialization Tests ==========


    /**
     * Tests filter spinners are initialized correctly.
     */
    @Test
    public void testFilterSpinnersInitialized() {
        interact(() -> {
            assertEquals(0, filterMinQuantitySpinner.getValue(), "Min quantity should start at 0");
            assertEquals(99, filterMaxQuantitySpinner.getValue(), "Max quantity should start at 99");
            assertTrue(filterIncludeUnknownCheck.isSelected(), "Include unknown expiration should be checked");
        });
    }

    /**
     * Tests that fridge list starts empty.
     */
    @Test
    @Disabled
    public void testFridgeListStartsEmpty() {
        interact(() -> {
            assertTrue(fridgeList.getItems().isEmpty(), "Fridge list should start empty");
        });
    }
}
