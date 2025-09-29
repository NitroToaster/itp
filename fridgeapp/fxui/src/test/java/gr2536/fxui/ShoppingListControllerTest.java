package gr2536.fxui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr2536.fxui.ShoppingListController.ShoppingItem;




/**
 * Basic tests for ShoppingListController functionality.
 * Tests the ShoppingItem class and basic validation logic.
 */
class ShoppingListControllerTest {

    private ShoppingListController.ShoppingItem item;

    @BeforeEach
    void setUp() {
        // Reset FridgeService before each test
        FridgeService.reset();
    }

    @Test
    void testShoppingItemCreation() {
        // Test valid shopping item creation
        ShoppingItem item = new ShoppingItem("Apple", 5);
        assertEquals("Apple", item.getName());
        assertEquals(5, item.getQuantity());
    }

    @Test
    void testShoppingItemWithTrimmedName() {
        // Test that names are trimmed
        ShoppingItem item = new ShoppingItem("  Banana  ", 3);
        assertEquals("Banana", item.getName());
        assertEquals(3, item.getQuantity());
    }

    @Test
    void testShoppingItemInvalidName() {
        // Test null name
        assertThrows(IllegalArgumentException.class, () -> {
            new ShoppingItem(null, 1);
        }, "Null name should throw IllegalArgumentException");

        // Test empty name
        assertThrows(IllegalArgumentException.class, () -> {
            new ShoppingItem("", 1);
        }, "Empty name should throw IllegalArgumentException");

        // Test whitespace-only name
        assertThrows(IllegalArgumentException.class, () -> {
            new ShoppingItem("   ", 1);
        }, "Whitespace-only name should throw IllegalArgumentException");
    }

    @Test
    void testShoppingItemInvalidQuantity() {
        // Test zero quantity
        assertThrows(IllegalArgumentException.class, () -> {
            new ShoppingItem("Orange", 0);
        }, "Zero quantity should throw IllegalArgumentException");

        // Test negative quantity
        assertThrows(IllegalArgumentException.class, () -> {
            new ShoppingItem("Orange", -1);
        }, "Negative quantity should throw IllegalArgumentException");
    }

    @Test
    void testShoppingItemToString() {
        // Test string representation
        ShoppingItem item = new ShoppingItem("Milk", 2);
        assertEquals("Milk - 2", item.toString());
        
        ShoppingItem singleItem = new ShoppingItem("Bread", 1);
        assertEquals("Bread - 1", singleItem.toString());
    }

    @Test
    void testShoppingItemEquality() {
        // Test that items with same data have same string representation
        ShoppingItem item1 = new ShoppingItem("Cheese", 3);
        ShoppingItem item2 = new ShoppingItem("Cheese", 3);
        
        assertEquals(item1.toString(), item2.toString(), 
                     "Items with same data should have same string representation");
        assertEquals(item1.getName(), item2.getName(), "Names should be equal");
        assertEquals(item1.getQuantity(), item2.getQuantity(), "Quantities should be equal");
    }

    @Test
    void testShoppingItemWithDifferentQuantities() {
        // Test items with same name but different quantities
        ShoppingItem item1 = new ShoppingItem("Eggs", 12);
        ShoppingItem item2 = new ShoppingItem("Eggs", 6);
        
        assertEquals("Eggs", item1.getName());
        assertEquals("Eggs", item2.getName());
        assertEquals(12, item1.getQuantity());
        assertEquals(6, item2.getQuantity());
        
        assertNotEquals(item1.toString(), item2.toString(), 
                        "Items with different quantities should have different string representations");
    }

    @Test
    void testShoppingItemEdgeCases() {
        // Test with very large quantity
        ShoppingItem item = new ShoppingItem("Rice", 99);
        assertEquals("Rice", item.getName());
        assertEquals(99, item.getQuantity());
        assertEquals("Rice - 99", item.toString());

        // Test with single character name
        ShoppingItem singleChar = new ShoppingItem("A", 1);
        assertEquals("A", singleChar.getName());
        assertEquals("A - 1", singleChar.toString());
    }

    @Test
    void testFridgeServiceIntegration() {
        // Test that we can access the fridge service (basic integration test)
        assertNotNull(FridgeService.getFridge(), "Should be able to access FridgeService");
        assertEquals(0, FridgeService.getFridge().listItems().size(), "Fridge should start empty");
    }
}
