package gr2536.core;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FridgeTest {

    private Fridge fridge;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;


    /**
     * Creates fridge for tests.
     */
    @BeforeEach
    public void fridgeSetup(){
        fridge = new Fridge();

        item1 = new Item("Milk", 1, LocalDate.of(2025, 9, 20));
        item2 = new Item("Milk", 2, LocalDate.of(2025, 9, 20));
        item3 = new Item("Butter", 2, LocalDate.of(2025, 9, 21));
        item4 = new Item("Juice", 1,LocalDate.of(2025, 9, 23));

        fridge.add(item1);
        fridge.add(item3);
    }

    /**
     * Test both that quantity stacks for equal items, and addition of new item.
     */
    @Test
    public void addTest(){
        assertEquals(List.of(
            new Item("Butter", 2, LocalDate.of(2025, 9, 21)),
            new Item("Milk", 1, LocalDate.of(2025, 9, 20))),
            fridge.listItems());
        fridge.add(item2);
        
        assertEquals( List.of(
            new Item("Butter", 2, LocalDate.of(2025, 9, 21)),
            new Item("Milk", 3, LocalDate.of(2025, 9, 20))),
            fridge.listItems());
        fridge.add(item4);

        assertEquals(List.of(
            new Item("Butter", 2, LocalDate.of(2025, 9, 21)),
            new Item("Juice", 1, LocalDate.of(2025, 9, 23)), 
            new Item("Milk", 3, LocalDate.of(2025, 9, 20))),
            fridge.listItems());
    }

    /**
     * Tests edge cases and general functionality of remove method.
     */
    @Test
    public void removeTest(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
        () -> fridge.remove("Milk", -1));
        assertEquals("quantityToRemove must be > 0", ex.getMessage());

        fridge.remove("Milk", 1);

        assertEquals(List.of(new Item("Butter", 2, LocalDate.of(2025, 9, 21))), fridge.listItems());

        fridge.add(item2);

        fridge.remove("Milk", 1);

        assertEquals(List.of(
            new Item("Butter", 2, LocalDate.of(2025, 9, 21)), 
            new Item("Milk", 1, LocalDate.of(2025, 9, 20))), 
            fridge.listItems());
    }


    /**
     * Tests basic functionality of getQuantity method.
     */
    @Test
    public void getQuantityTest(){
        assertEquals(1, (int) fridge.getQuantity("Milk"));

        fridge.add(item2);

        assertEquals(3, (int) fridge.getQuantity("Milk"));
    }
        

}
