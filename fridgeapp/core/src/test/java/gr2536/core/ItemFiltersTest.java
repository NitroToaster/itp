package gr2536.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

public class ItemFiltersTest {

    /*
     * Test that passing null SearchCriteria to predicate() returns a predicate that always returns true.
     */
    @Test
    public void predicateHandleNullCriteria() {
        Predicate<Item> p = ItemFilters.predicate(null);
        assertTrue(p.test(new Item("Milk", 1, null))); 
    }

    /*
     * Test that predicate() correctly filters items by name using CONTAINS mode.
     */
    @Test
    public void predicateFiltersByQuantityAndExpiration() {
        LocalDate date = LocalDate.of(2025, 10, 1);
        Item item1 = new Item("Milk", 1, date.plusDays(1));
        Item item2 = new Item("Cheese", 5, date.plusDays(10));
        SearchCriteria criteria = new SearchCriteria("milk", NameMatchMode.CONTAINS, 1, 5, date, date.plusDays(5), true, SearchSort.DEFAULT);

        Predicate<Item> p = ItemFilters.predicate(criteria);

        assertTrue(p.test(item1));
        assertFalse(p.test(item2));
    }

    /*
     * Test that comparators sort items correctly according to different sort orders.
     */
    @Test
    public void comparatorSelectsCorrectComparator() {
        assertEquals(ItemListUtils.ITEM_ORDER, ItemFilters.comparator(null));

        SearchCriteria cNullSort = new SearchCriteria(null, null, null, null, null, null, true, null);
        assertEquals(ItemListUtils.ITEM_ORDER, ItemFilters.comparator(cNullSort));

        SearchCriteria cDefault = new SearchCriteria(null, null, null, null, null, null, true, SearchSort.DEFAULT);
        assertEquals(ItemListUtils.ITEM_ORDER, ItemFilters.comparator(cDefault));

        assertEquals(ItemListUtils.BY_NAME_ASC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.NAME_ASC)));

        assertEquals(ItemListUtils.BY_NAME_DESC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.NAME_DESC)));

        assertEquals(ItemListUtils.BY_EXPIRATION_ASC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.EXPIRATION_ASC)));

        assertEquals(ItemListUtils.BY_EXPIRATION_DESC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.EXPIRATION_DESC)));

        assertEquals(ItemListUtils.BY_QUANTITY_ASC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.QUANTITY_ASC)));

        assertEquals(ItemListUtils.BY_QUANTITY_DESC, ItemFilters.comparator(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.QUANTITY_DESC)));
    }


    



    /*
     * Test that predicate() includes items with unknown expiration dates when includeUnknownExpiration is true.
     */
    @Test
    public void predicateIncludesUnknownExpirationWhenTrue() {
        Item unknown = new Item("Egg", 1, null);
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, LocalDate.of(2025, 10, 1), null, true, SearchSort.DEFAULT);
        Predicate<Item> p = ItemFilters.predicate(criteria);
        assertTrue(p.test(unknown));
    }

    /*
     * Test that predicate() excludes items with unknown expiration dates when includeUnknownExpiration is false.
     */
    @Test
    public void predicateExcludesUnknownExpirationWhenFalse() {
        Item unknown = new Item("Egg", 1, null);
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, LocalDate.of(2025, 10, 1), null, false, SearchSort.DEFAULT);
        Predicate<Item> p = ItemFilters.predicate(criteria);
        assertFalse(p.test(unknown));
    }

}
