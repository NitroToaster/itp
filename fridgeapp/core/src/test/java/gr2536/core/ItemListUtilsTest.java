package gr2536.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import gr2536.core.utils.*;
import gr2536.core.item.Item;

public class ItemListUtilsTest {

    /*
     * Tests clampToIntMax method.
     */
    @Test
    public void clampToIntMaxTest() {
        assertEquals(42, ItemListUtils.clampToIntMax(42));
        assertEquals(Integer.MAX_VALUE, ItemListUtils.clampToIntMax((long) Integer.MAX_VALUE + 100));  
    }

    /*
     * Tests clampAdd method.
     */
    @Test
    public void clampAddTest() {
        assertEquals(5, ItemListUtils.clampAdd(2, 3));
        assertEquals(Integer.MAX_VALUE, ItemListUtils.clampAdd(Integer.MAX_VALUE, 10));
    }

    /*
     * Tests nameMatches method.
     */
    @Test
    public void nameMatchesTest() {
        assertTrue(ItemListUtils.nameMatches("Milk", null, NameMatchMode.CONTAINS));

        assertTrue(ItemListUtils.nameMatches("Milk", "milk", NameMatchMode.EXACT));
        assertFalse(ItemListUtils.nameMatches("Milkshake", "milk", NameMatchMode.EXACT));
        assertFalse(ItemListUtils.nameMatches(null, "milk", NameMatchMode.EXACT));

        assertTrue(ItemListUtils.nameMatches("Milkshake", "milk", NameMatchMode.PREFIX));
        assertFalse(ItemListUtils.nameMatches("Cream", "milk", NameMatchMode.PREFIX));
        assertFalse(ItemListUtils.nameMatches(null, "milk", NameMatchMode.PREFIX));

        assertTrue(ItemListUtils.nameMatches("Chocolate milk", "milk", NameMatchMode.CONTAINS));
        assertFalse(ItemListUtils.nameMatches("Cheese", "milk", NameMatchMode.CONTAINS));
        assertFalse(ItemListUtils.nameMatches(null, "milk", NameMatchMode.CONTAINS));

        assertTrue(ItemListUtils.nameMatches("Choco milk", "milk", null));

        assertTrue(ItemListUtils.nameMatches("Anything", "", NameMatchMode.EXACT));
    }

    /*
     * Tests nameMatchesAll method.
     */
    @Test
    public void nameMatchesAllTest() {
        assertTrue(ItemListUtils.nameMatchesAll("Milk", null));
        assertTrue(ItemListUtils.nameMatchesAll("Milk", " "));
        assertFalse(ItemListUtils.nameMatchesAll(null, "milk"));
        assertTrue(ItemListUtils.nameMatchesAll("Milk", "milk"));
        assertTrue(ItemListUtils.nameMatchesAll("Milkshake", "milk"));
        assertTrue(ItemListUtils.nameMatchesAll("Chocolate milk", "milk"));
        assertFalse(ItemListUtils.nameMatchesAll("Cheese", "milk"));
    }

    /*
     * Tests the comparators.
     */
    @Test
    public void comparatorsTest() {
        Item item1 = new Item("Milk", 2, LocalDate.now().plusDays(5));
        Item item2 = new Item("milk", 5, LocalDate.now().plusDays(13));
        Item item3 = new Item("Cheese", 1, null);
        Item item4 = new Item("Apple", 2, LocalDate.now().plusDays(2));

        List<Item> itemList = new ArrayList<>(List.of(item2, item3, item1, item4));

        itemList.sort(ItemListUtils.ITEM_ORDER);
        assertEquals(List.of(item4, item3, item1, item2), itemList);

        itemList.sort(ItemListUtils.BY_NAME_DESC);
        assertEquals(List.of(item1, item2, item3, item4), itemList);

        itemList.sort(ItemListUtils.BY_EXPIRATION_ASC);
        assertEquals(List.of(item4, item1, item2, item3), itemList);

        itemList.sort(ItemListUtils.BY_EXPIRATION_DESC);
        assertEquals(List.of(item2, item1, item4, item3), itemList);

        itemList.sort(ItemListUtils.BY_QUANTITY_ASC);
        assertEquals(List.of(item3, item4, item1, item2), itemList);

        itemList.sort(ItemListUtils.BY_QUANTITY_DESC);
        assertEquals(List.of(item2, item4, item1, item3), itemList);

        LocalDate sharedDate = LocalDate.of(2025, 10, 20);
        Item a = new Item("Banana", 1, sharedDate);
        Item b = new Item("Apple", 1, sharedDate);
        Item c = new Item("Zucchini", 1, null);

        List<Item> itemList2 = new ArrayList<>(List.of(a, b, c));
        itemList2.sort(ItemListUtils.BY_EXPIRATION_DESC);
        assertEquals(List.of(b, a, c), itemList2);

        itemList2.sort(ItemListUtils.BY_EXPIRATION_ASC);
        assertEquals(List.of(b, a, c), itemList2);
    }

    /*
     * Tests Key class.
     */
    @Test
    public void keyOfValidAndInvalidTest() {
        Key key1 = Key.of("Milk");
        assertEquals("milk", key1.name());
        assertEquals(new Key("milk"), key1);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, 
            () -> Key.of(null));
        assertEquals("Name must be non-empty", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, 
            () -> Key.of("   "));
        assertEquals("Name must be non-empty", ex2.getMessage());
    }

    /*
     * Tests Entry class.
     */
    @Test
    public void entryStoresQuantitiesInOrderTest() {
        Entry e = new Entry("Eggs");
        e.quantitiesByExpiration.put(LocalDate.of(2025, 5, 1), 1);
        e.quantitiesByExpiration.put(LocalDate.of(2025, 4, 1), 2);
        e.quantitiesByExpiration.put(null, 3);

        List<LocalDate> dates = new ArrayList<>(e.quantitiesByExpiration.keySet());
        assertIterableEquals(Arrays.asList(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1), null), dates);
        assertEquals("Eggs", e.displayName);
    }

}
