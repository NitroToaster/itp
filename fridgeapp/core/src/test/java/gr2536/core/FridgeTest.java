package gr2536.core;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        

    // ---------------- SEARCH/FILTER TESTS ----------------

    @Test
    public void searchByNameContainsTest() {
        List<Item> result = fridge.search(new SearchCriteria("mi", NameMatchMode.CONTAINS, null, null, null, null, true, SearchSort.DEFAULT));
        assertEquals(List.of(new Item("Milk", 1, LocalDate.of(2025, 9, 20))), result);
    }

    @Test
    public void searchByMinQuantityTest() {
        List<Item> result = fridge.search(new SearchCriteria(null, null, 2, null, null, null, true, null));
        assertEquals(List.of(new Item("Butter", 2, LocalDate.of(2025, 9, 21))), result);
    }

    @Test
    public void searchByExpirationFromTest() {
        List<Item> result = fridge.search(new SearchCriteria(null, null, null, null, LocalDate.of(2025, 9, 21), null, true, null));
        assertEquals(List.of(new Item("Butter", 2, LocalDate.of(2025, 9, 21))), result);
    }

    @Test
    public void searchSortByExpirationDescTest() {
        fridge.add(item2);
        fridge.add(item4);
        List<Item> result = fridge.search(new SearchCriteria(null, null, null, null, null, null, true, SearchSort.EXPIRATION_DESC));
        assertEquals(List.of(
                new Item("Juice", 1, LocalDate.of(2025, 9, 23)),
                new Item("Butter", 2, LocalDate.of(2025, 9, 21)),
                new Item("Milk", 3, LocalDate.of(2025, 9, 20))
        ), result);
    }

    // ---------------- FILTER TESTS ----------------

    /**
     * Tests filterByQuantityAtLeast method with various minimum values.
     */
    @Test
    public void filterByQuantityAtLeastTest() {
        fridge.add(item2); // Add more milk (total 3)
        fridge.add(item4); // Add juice (1)
        
        // Filter for items with at least 2 units
        List<Item> result = fridge.filterByQuantityAtLeast(2);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> item.getQuantity() >= 2));
    }

    /**
     * Tests filterByQuantityAtLeast with zero returns all items.
     */
    @Test
    public void filterByQuantityAtLeastZeroTest() {
        fridge.add(item2);
        List<Item> result = fridge.filterByQuantityAtLeast(0);
        assertEquals(2, result.size());
    }

    /**
     * Tests filterByQuantityAtLeast with negative value throws exception.
     */
    @Test
    public void filterByQuantityAtLeastNegativeThrowsTest() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> fridge.filterByQuantityAtLeast(-1)
        );
        assertEquals("min must be >= 0", ex.getMessage());
    }

    /**
     * Tests filterByQuantityAtMost method with various maximum values.
     */
    @Test
    public void filterByQuantityAtMostTest() {
        fridge.add(item2); // Add more milk (total 3)
        fridge.add(item4); // Add juice (1)
        
        // Filter for items with at most 1 unit
        List<Item> result = fridge.filterByQuantityAtMost(1);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(item -> item.getQuantity() <= 1));
        assertEquals("Juice", result.get(0).getName());
    }

    /**
     * Tests filterByQuantityAtMost with large value returns all items.
     */
    @Test
    public void filterByQuantityAtMostLargeValueTest() {
        fridge.add(item2);
        List<Item> result = fridge.filterByQuantityAtMost(100);
        assertEquals(2, result.size());
    }

    /**
     * Tests filterByQuantityAtMost with negative value throws exception.
     */
    @Test
    public void filterByQuantityAtMostNegativeThrowsTest() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> fridge.filterByQuantityAtMost(-1)
        );
        assertEquals("max must be >= 0", ex.getMessage());
    }

    /**
     * Tests filterByExpirationFrom method with date range.
     */
    @Test
    public void filterByExpirationFromTest() {
        fridge.add(item4); // Juice expires on 9/23
        
        // Filter for items expiring from Sep 21 onwards
        List<Item> result = fridge.filterByExpirationFrom(LocalDate.of(2025, 9, 21));
        assertEquals(2, result.size());
        assertTrue(result.stream()
            .allMatch(item -> !item.getExpirationDate().isBefore(LocalDate.of(2025, 9, 21))));
    }

    /**
     * Tests filterByExpirationFrom with future date returns no items.
     */
    @Test
    public void filterByExpirationFromFutureDateTest() {
        List<Item> result = fridge.filterByExpirationFrom(LocalDate.of(2026, 1, 1));
        assertTrue(result.isEmpty());
    }

    /**
     * Tests filterByExpirationFrom with null throws exception.
     */
    @Test
    public void filterByExpirationFromNullThrowsTest() {
        assertThrows(
            NullPointerException.class,
            () -> fridge.filterByExpirationFrom(null)
        );
    }

    /**
     * Tests filterByExpirationUntil method with date range.
     */
    @Test
    public void filterByExpirationUntilTest() {
        fridge.add(item2); // More milk on 9/20
        fridge.add(item4); // Juice on 9/23
        
        // Filter for items expiring until Sep 20
        List<Item> result = fridge.filterByExpirationUntil(LocalDate.of(2025, 9, 20));
        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
        assertFalse(result.get(0).getExpirationDate().isAfter(LocalDate.of(2025, 9, 20)));
    }

    /**
     * Tests filterByExpirationUntil with past date returns no items.
     */
    @Test
    public void filterByExpirationUntilPastDateTest() {
        List<Item> result = fridge.filterByExpirationUntil(LocalDate.of(2025, 1, 1));
        assertTrue(result.isEmpty());
    }

    /**
     * Tests filterByExpirationUntil with null throws exception.
     */
    @Test
    public void filterByExpirationUntilNullThrowsTest() {
        assertThrows(
            NullPointerException.class,
            () -> fridge.filterByExpirationUntil(null)
        );
    }

    /**
     * Tests search with combined criteria: name, quantity, and date filters.
     */
    @Test
    public void searchCombinedCriteriaTest() {
        fridge.add(item2); // More milk (total 3)
        fridge.add(item4); // Juice (1)
        
        // Search for items with "i" in name, at least 2 units, expiring after Sep 19
        SearchCriteria criteria = new SearchCriteria(
            "i", 
            NameMatchMode.CONTAINS, 
            2, 
            null, 
            LocalDate.of(2025, 9, 19), 
            null, 
            true, 
            SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
        assertTrue(result.get(0).getQuantity() >= 2);
    }

    /**
     * Tests that search with null criteria returns all items.
     */
    @Test
    public void searchWithNullCriteriaTest() {
        fridge.add(item2);
        fridge.add(item4);
        
        List<Item> result = fridge.search(null);
        assertEquals(3, result.size());
    }

    /**
     * Tests search with empty name query returns all items.
     */
    @Test
    public void searchWithEmptyNameQueryTest() {
        fridge.add(item4);
        
        SearchCriteria criteria = new SearchCriteria(
            "", 
            NameMatchMode.CONTAINS, 
            null, 
            null, 
            null, 
            null, 
            true, 
            SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(3, result.size()); // Butter, Juice, and Milk from setup
    }

    /**
     * Tests search with quantity range.
     */
    @Test
    public void searchWithQuantityRangeTest() {
        fridge.add(item2); // Milk total 3
        fridge.add(item4); // Juice 1
        
        // Search for items with 1-2 units
        SearchCriteria criteria = new SearchCriteria(
            null, 
            NameMatchMode.CONTAINS, 
            1, 
            2, 
            null, 
            null, 
            true, 
            SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(item -> 
            item.getQuantity() >= 1 && item.getQuantity() <= 2));
    }

    /**
     * Tests search with date range.
     */
    @Test
    public void searchWithDateRangeTest() {
        fridge.add(item2);
        fridge.add(item4);
        
        // Search for items expiring between Sep 20 and Sep 22
        SearchCriteria criteria = new SearchCriteria(
            null, 
            NameMatchMode.CONTAINS, 
            null, 
            null, 
            LocalDate.of(2025, 9, 20), 
            LocalDate.of(2025, 9, 22), 
            true, 
            SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(2, result.size()); // Milk and Butter
    }

    /**
     * Tests search sort by name ascending.
     */
    @Test
    public void searchSortByNameAscTest() {
        fridge.add(item4);
        
        SearchCriteria criteria = new SearchCriteria(
            null, null, null, null, null, null, true, SearchSort.NAME_ASC
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(3, result.size());
        assertEquals("Butter", result.get(0).getName());
        assertEquals("Juice", result.get(1).getName());
        assertEquals("Milk", result.get(2).getName());
    }

    /**
     * Tests search sort by name descending.
     */
    @Test
    public void searchSortByNameDescTest() {
        fridge.add(item4);
        
        SearchCriteria criteria = new SearchCriteria(
            null, null, null, null, null, null, true, SearchSort.NAME_DESC
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(3, result.size());
        assertEquals("Milk", result.get(0).getName());
        assertEquals("Juice", result.get(1).getName());
        assertEquals("Butter", result.get(2).getName());
    }

    /**
     * Tests search with name match mode EXACT.
     */
    @Test
    public void searchNameExactModeTest() {
        SearchCriteria criteria = new SearchCriteria(
            "Milk", NameMatchMode.EXACT, null, null, null, null, true, SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
    }

    /**
     * Tests search with name match mode PREFIX.
     */
    @Test
    public void searchNamePrefixModeTest() {
        fridge.add(item4);
        
        SearchCriteria criteria = new SearchCriteria(
            "Mi", NameMatchMode.PREFIX, null, null, null, null, true, SearchSort.DEFAULT
        );
        
        List<Item> result = fridge.search(criteria);
        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
    }
}
