package gr2536.core;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class SearchCriteriaTest {

    /*
     * Test for the static factory method SearchCriteria.of()
     */
    @Test
    public void of_returnsDefaultCriteria() {
        SearchCriteria criteria = SearchCriteria.of();
        assertNull(criteria.nameQuery());
        assertEquals(NameMatchMode.CONTAINS, criteria.nameMode());
        assertNull(criteria.minQuantity());
        assertNull(criteria.maxQuantity());
        assertNull(criteria.expirationFrom());
        assertNull(criteria.expirationUntil());
        assertTrue(criteria.includeUnknownExpiration());
        assertEquals(SearchSort.DEFAULT, criteria.sort()); 
    }

    /*
     * Tests for the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_trimsNameQuery() {
        SearchCriteria criteria1 = new SearchCriteria("  apple  ", null, null, null, null, null, true, null);
        assertEquals("apple", criteria1.nameQuery());

        SearchCriteria criteria2 = new SearchCriteria("     ", null, null, null, null, null, true, null);
        assertNull(criteria2.nameQuery());

        SearchCriteria criteria3 = new SearchCriteria(null, null, null, null, null, null, true, null);
        assertNull(criteria3.nameQuery());
    }

    /*
     * Tests for default values of nameMode and sort in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_defaultsForNameModeAndSort() {
        SearchCriteria criteria = new SearchCriteria(null, null, null, null, null, null, true, null);
        assertEquals(NameMatchMode.CONTAINS, criteria.nameMode());
        assertEquals(SearchSort.DEFAULT, criteria.sort());
    }

    /*
     * Tests for provided values of nameMode and sort in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_keepsProvidedNameModeAndSort() {
        SearchCriteria criteria = new SearchCriteria("pear", NameMatchMode.EXACT, 1, 5, null, null, true, SearchSort.NAME_ASC);
        assertEquals("pear", criteria.nameQuery());
        assertEquals(NameMatchMode.EXACT, criteria.nameMode());
        assertEquals(SearchSort.NAME_ASC, criteria.sort());
    }

    /*
     * Test that all fields are set correctly in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_setsAllFieldsCorrectly() {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate untilDate = LocalDate.of(2025, 12, 31);
        SearchCriteria criteria = new SearchCriteria("banana", NameMatchMode.PREFIX, 10, 20, fromDate, untilDate, false, SearchSort.QUANTITY_DESC);

        assertEquals("banana", criteria.nameQuery());
        assertEquals(NameMatchMode.PREFIX, criteria.nameMode());
        assertEquals(10, criteria.minQuantity());
        assertEquals(10, criteria.minQuantity());
        assertEquals(20, criteria.maxQuantity());
        assertEquals(fromDate, criteria.expirationFrom());
        assertEquals(untilDate, criteria.expirationUntil());
        assertFalse(criteria.includeUnknownExpiration());
        assertEquals(SearchSort.QUANTITY_DESC, criteria.sort());
    }

    /*
     * Tests if minQuantity is negative in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_throwsIfMinQuantityNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SearchCriteria(null, null, -1, null, null, null, true, null);
        });
        assertEquals("minQuantity must be >= 0", exception.getMessage());
    }

    /*
     * Tests if maxQuantity is negative in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_throwsIfMaxQuantityNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SearchCriteria(null, null, null, -5, null, null, true, null);
        });
        assertEquals("maxQuantity must be >= 0", exception.getMessage());
    }

    /*
     * Tests if minQuantity is greater than maxQuantity in the constructor SearchCriteria(...)
     */
    @Test
    public void constructor_throwsIfMinQuantityGreaterThanMaxQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SearchCriteria(null, null, 10, 5, null, null, true, null);
        });
        assertEquals("minQuantity must be <= maxQuantity", exception.getMessage());
    }  
}
