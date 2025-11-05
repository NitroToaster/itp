package gr2536.core.utils;

import java.time.LocalDate;

public record SearchCriteria(
        String nameQuery,
        NameMatchMode nameMode,
        Integer minQuantity,
        Integer maxQuantity,
        LocalDate expirationFrom,
        LocalDate expirationUntil,
        boolean includeUnknownExpiration,
        SearchSort sort) {

    public SearchCriteria {
        if (nameQuery != null) {
            String trimmed = nameQuery.trim();
            nameQuery = trimmed.isEmpty() ? null : trimmed;
        }
        if (nameMode == null) {
            nameMode = NameMatchMode.CONTAINS;
        }
        if (sort == null) {
            sort = SearchSort.DEFAULT;
        }
        if (minQuantity != null && minQuantity < 0) {
            throw new IllegalArgumentException("minQuantity must be >= 0");
        }
        if (maxQuantity != null && maxQuantity < 0) {
            throw new IllegalArgumentException("maxQuantity must be >= 0");
        }
        if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) {
            throw new IllegalArgumentException("minQuantity must be <= maxQuantity");
        }
    }

    public static SearchCriteria of() {
        return new SearchCriteria(null, NameMatchMode.CONTAINS, null, null, null, null, true, SearchSort.DEFAULT);
    }
}

