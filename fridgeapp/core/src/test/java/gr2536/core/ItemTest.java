package gr2536.core;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
        item = new Item("Milk", 1, LocalDate.of(2025, 9, 20));
        assertEquals("Milk", item.getName());
        assertEquals(1 ,item.getQuantity());
        assertEquals(LocalDate.of(2025, 9, 20), item.getExpirationDate());
    }

    /**
     * Checks that the constructor throws with the correct message.
     */
    @Test
    public void constructorThrowsTest(){
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> 
            new Item("", 1, LocalDate.of(2025, 9, 20))
            );
            assertEquals(ex1.getMessage(), "name must be non-empty");

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> 
            new Item("Milk", -1, LocalDate.of(2025, 9, 20))
            );
            assertEquals(ex2.getMessage(), "quantity must be > 0");
        
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () -> 
            new Item(null, 1, LocalDate.of(2025, 9, 20))
            );
            assertEquals(exNull.getMessage(), "name must be non-empty");
    }

    @Test
    public void equalsAndHashCodeTest(){
        Item item1 = new Item("Milk", 1, LocalDate.now().plusDays(5));
        Item item2 = new Item("milk", 1, LocalDate.now().plusDays(5));
        Item item3 = new Item("Milk", 2, LocalDate.now().plusDays(5));
        Item item4 = new Item("Milk", 1, null);
        Item item5 = new Item("Egg", 1, LocalDate.now().plusDays(5));

        assertTrue(item1.equals(item2));
        assertTrue(!item1.equals(item3));
        assert(!item1.equals(item4));
        assertTrue(!item1.equals(item5));
        assertTrue(!item1.equals(null));
        assertTrue(!item1.equals("string"));
        assertTrue(item1.equals(item1));
        
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());
    }

    @Test
    public void withQuantityTest(){
        Item item1 = new Item("Cheese", 1, LocalDate.now().plusDays(7));
        Item item2 = item1.withQuantity(5);

        assertEquals("Cheese", item2.getName());
        assertEquals(5, item2.getQuantity());
        assertEquals(LocalDate.now().plusDays(7),item2.getExpirationDate());

        assertNotSame(item1, item2);
        assertEquals(1, item1.getQuantity());
    }

    @Test
    public void toStringTest(){
        Item item1 = new Item("Butter", 3, LocalDate.of(2025, 11, 15));
        String str = item1.toString();

        assertTrue(str.contains("Butter"));
        assertTrue(str.contains("3"));
        assertTrue(str.contains("2025-11-15"));
        assertTrue(str.startsWith("Item{"));
    }

}
