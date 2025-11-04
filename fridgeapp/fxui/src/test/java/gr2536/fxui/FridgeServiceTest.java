package gr2536.fxui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr2536.core.fridge.Fridge;
import gr2536.core.item.Item;
import java.time.LocalDate;

/**
 * Basic tests for FridgeService functionality.
 * Tests the shared service that manages Fridge instances across controllers.
 */
class FridgeServiceTest {

    
    @BeforeEach
    void setUp() {
        // Reset to clean state before each test
        FridgeService.reset();
    }

    @Test
    void testGetFridge() {
        // Test that we get a valid Fridge instance
        Fridge fridge = FridgeService.getFridge();
        assertNotNull(fridge, "getFridge() should return a non-null Fridge");
        
        // Test that it's the same instance on multiple calls
        Fridge fridge2 = FridgeService.getFridge();
        assertSame(fridge, fridge2, "getFridge() should return the same instance");
    }

    @Test
    void testSetFridge() {
        // Create a new fridge with some data
        Fridge newFridge = new Fridge();
        Item testItem = new Item("Test Item", 5, LocalDate.now().plusDays(3));
        newFridge.add(testItem);
        
        // Set the new fridge
        FridgeService.setFridge(newFridge);
        
        // Verify it's now the active fridge
        Fridge retrievedFridge = FridgeService.getFridge();
        assertSame(newFridge, retrievedFridge, "setFridge() should update the active fridge");
        assertEquals(1, retrievedFridge.listItems().size(), "New fridge should contain the test item");
    }

    @Test
    void testSetFridgeWithNull() {
        // Test that setting null throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            FridgeService.setFridge(null);
        }, "setFridge(null) should throw IllegalArgumentException");
    }

    @Test
    void testReset() {
        // Add some data to the current fridge
        Fridge fridge = FridgeService.getFridge();
        Item testItem = new Item("Test Item", 3, LocalDate.now().plusDays(2));
        fridge.add(testItem);
        assertEquals(1, fridge.listItems().size(), "Fridge should contain test item");
        
        // Reset the service
        FridgeService.reset();
        
        // Verify we have a new empty fridge
        Fridge newFridge = FridgeService.getFridge();
        assertNotSame(fridge, newFridge, "reset() should create a new Fridge instance");
        assertEquals(0, newFridge.listItems().size(), "New fridge should be empty");
    }

    @Test
    void testPersistenceAcrossOperations() {
        // Test that data persists across multiple operations
        Fridge fridge = FridgeService.getFridge();
        
        // Add multiple items
        Item item1 = new Item("Apple", 5, LocalDate.now().plusDays(7));
        Item item2 = new Item("Bread", 2, LocalDate.now().plusDays(3));
        fridge.add(item1);
        fridge.add(item2);
        
        // Verify items persist when getting fridge again
        Fridge sameFridge = FridgeService.getFridge();
        assertEquals(2, sameFridge.listItems().size(), "Fridge should maintain its data");
        assertTrue(sameFridge.listItems().stream().anyMatch(i -> "Apple".equals(i.getName())), 
                   "Fridge should contain Apple");
        assertTrue(sameFridge.listItems().stream().anyMatch(i -> "Bread".equals(i.getName())), 
                   "Fridge should contain Bread");
    }
}