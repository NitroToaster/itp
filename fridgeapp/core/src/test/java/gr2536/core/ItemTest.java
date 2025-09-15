package gr2536.core;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ItemTest {
    
    private Item item;

    
    /**
     * Tests that the constructor creates the object,
     * and that the getters work as intended.
     */
    @Test
    public void constructorAndGettersTest(){
        item = new Item("Milk", 1, "Drink", LocalDate.of(2025, 9, 20));
        assertEquals("Milk", item.getName());
        assertEquals(1 ,item.getQuantity());
        assertEquals("Drink", item.getUnit());
        assertEquals(LocalDate.of(2025, 9, 20), item.getExpirationDate());
    }

    /**
     * Checks that the constructor throws with the correct message.
     */
    @Test
    public void constructorThrowsTest(){
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> 
            new Item("", 1, "Drink", LocalDate.of(2025, 9, 20))
            );
            assertEquals(ex1.getMessage(), "name must be non-empty");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> 
            new Item("Milk", -1, "Drink", LocalDate.of(2025, 9, 20))
            );
            assertEquals(ex2.getMessage(), "quantity must be > 0");
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> 
            new Item("Milk", 1, "", LocalDate.of(2025, 9, 20))
            );
            assertEquals(ex3.getMessage(), "unit must be non-empty");
    }

    /**
     * Checks basic functionality of boolean equals method.
     */
    @Test
    public void booleanTest(){
        Item item1 = new Item("Milk", 1, "Drink", LocalDate.of(2025, 9, 20));
        Item item2 = new Item("Milk", 1, "Drink", LocalDate.of(2025, 9, 20));
        assertTrue(item1.equals(item2));
    }

    
    
}
