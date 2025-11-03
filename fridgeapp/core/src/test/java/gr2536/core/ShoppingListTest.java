package gr2536.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr2536.core.fridge.Fridge;
import gr2536.core.item.Item;
import gr2536.core.shoppingList.ShoppingList;
import gr2536.core.utils.NameMatchMode;
import gr2536.core.utils.SearchCriteria;

public class ShoppingListTest {
    private Fridge fridge;
    private ShoppingList sList;
    private Item item1;
    private Item item2;
    private Item item3;
    private Item item4;
    private Item item5;

    /**
     * Creates a fridge for the tests
     */
    @BeforeEach
    public void createFridge() {
        fridge = new Fridge();
        sList = new ShoppingList();

        item1 = new Item("Cheese", 1, null);
        item2 = new Item("Egg", 2, LocalDate.of(2025, 9, 30));
        item3 = new Item("Fish", 4, LocalDate.of(2025, 10, 3));
        item4 = new Item("Egg", 1, LocalDate.of(2025, 10, 14));
        item5 = new Item("Fish", 1, LocalDate.of(2025, 10, 3));
    }

    /**
     * Tests the add method
     */
    @Test
    public void addToListTest() {
        assertTrue(sList.listItems().isEmpty());
        sList.add(item1);

        assertEquals(List.of(
                item1),
                sList.listItems());
        sList.add(item2);

        assertEquals(List.of(
                item1,
                item2),
                sList.listItems());
        sList.add(item3);

        assertEquals(List.of(
                item1,
                item2,
                item3),
                sList.listItems());
        sList.add(item4);

        assertEquals(List.of(
                item1,
                item2,
                item4,
                item3),
                sList.listItems());
        sList.add(item5);

        assertEquals(List.of(
                item1,
                item2,
                item4,
                new Item("Fish", 5, LocalDate.of(2025, 10, 3))

        ),
                sList.listItems());
    }

    /**
     * Test the remove metod, and correct handling of illegal argument
     */
    @Test
    public void removeFromListTest() {
        int result = sList.remove("Milk", 1);
        assertEquals(0, result);

        sList.add(item2);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sList.remove("Egg", -1));
        assertEquals("quantityToRemove must be > 0", ex.getMessage());

        sList.remove("Egg", 1);
        assertEquals(List.of(new Item("Egg", 1, LocalDate.of(2025, 9, 30))), sList.listItems());

        sList.add(item1);
        sList.remove("Egg", 1);
        assertEquals(List.of(
                new Item("Cheese", 1, null)),
                sList.listItems());

        sList.add(item2);
        int remaining = sList.remove("Egg", 5);
        assertEquals(0, remaining);
        assertEquals(List.of(new Item("Cheese", 1, null)), sList.listItems());
    }

    /**
     * Tests the getQuantity method
     */
    @Test
    public void getQuantityTest() {
        assertEquals(0, sList.getQuantity("Unknown"));

        sList.add(item3);
        assertEquals(4, sList.getQuantity("Fish"));

        sList.add(item5);
        assertEquals(5, sList.getQuantity("Fish"));
    }

    /**
     * Tests that the addToFridge method adds all items from the shopping list to
     * the fridge,
     * and clears the shopping list
     */
    @Test
    public void addToFridgeTest() {
        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        sList.addToFridge(fridge);

        assertTrue(sList.listItems().isEmpty());

        assertEquals(List.of(
                item1,
                item2,
                item4,
                new Item("Fish", 5, LocalDate.of(2025, 10, 3))),
                fridge.listItems());
    }

    
    /*
     * Tests the searchByNameExact method
     */
    @Test
    public void searchByNameExactTest() {
        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        List<Item> result = sList.search(new SearchCriteria("Egg", NameMatchMode.EXACT, null, null, null, null, true, null));
        assertEquals(List.of(
                new Item("Egg", 2, LocalDate.of(2025, 9, 30)),
                new Item("Egg", 1, LocalDate.of(2025, 10, 14))
        ), result);

        List<Item> nullSearchResult = sList.search(null);
        assertEquals(sList.listItems(), nullSearchResult);
    }

    /*
     * Tests the filterByMinQuantity method
     */
    @Test
    public void filterByMinQuantityTest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sList.filterByQuantityAtLeast(-1));
        assertEquals("min must be >= 0", ex.getMessage());

        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        List<Item> result = sList.filterByQuantityAtLeast(3);
        assertEquals(List.of(new Item("Fish", 5, LocalDate.of(2025, 10, 3))), result);
    }

    /*
     * Tests the expirationUntil method with includeUnknown flag set to true and false
     */
    @Test
    public void expirationUntilIncludeUnknownTest() {
        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        List<Item> result = sList.search(new SearchCriteria(null, null, null, null, null, LocalDate.of(2025, 9, 30), true, null));
        assertEquals(List.of(
                new Item("Cheese", 1, null),
                new Item("Egg", 2, LocalDate.of(2025, 9, 30))
        ), result);
    }

    /*
     * Tests the expirationUntil method with includeUnknown flag set to false
     */
    @Test
    public void expirationUntilExcludeUnknownTest() {
        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        List<Item> result = sList.search(new SearchCriteria(null, null, null, null, null, LocalDate.of(2025, 9, 30), false, null));
        assertEquals(List.of(new Item("Egg", 2, LocalDate.of(2025, 9, 30))), result);
    }

    /*
     * Tests the filterByQuantityAtMost method
     */
    @Test
    public void filterByQuantityAtMostTest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> sList.filterByQuantityAtMost(-5));
        assertEquals("max must be >= 0", ex.getMessage());

        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item5);

        List<Item> result = sList.filterByQuantityAtMost(2);
        assertEquals(List.of(
                new Item("Cheese", 1, null),
                new Item("Egg", 2, LocalDate.of(2025, 9, 30))
                ), result);
    }

    /*
     * Tests the filterByExpirationFrom and filterByExpirationUntil methods
     */
    @Test
    public void filterByExpirationFromAndUntilTest() {
        sList.add(item1);
        sList.add(item2);
        sList.add(item3);
        sList.add(item4);
        sList.add(item5);

        List<Item> fromResult = sList.filterByExpirationFrom(LocalDate.of(2025, 10, 1));
        assertEquals(List.of(
                new Item("Cheese", 1, null),
                new Item("Egg", 1, LocalDate.of(2025, 10, 14)),
                new Item("Fish", 5, LocalDate.of(2025, 10, 3))
                ), fromResult);

        List<Item> untilResult  = sList.filterByExpirationUntil(LocalDate.of(2025, 10, 3));
        assertEquals(List.of(
                new Item("Cheese", 1, null),
                new Item("Egg", 2, LocalDate.of(2025, 9, 30)),
                new Item("Fish", 5, LocalDate.of(2025, 10, 3))
        ), untilResult);

        assertThrows(NullPointerException.class, () -> sList.filterByExpirationFrom(null));
        assertThrows(NullPointerException.class, () -> sList.filterByExpirationUntil(null));
    }


}