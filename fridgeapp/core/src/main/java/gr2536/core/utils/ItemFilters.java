package gr2536.core.utils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Predicate;

import gr2536.core.item.Item;

public final class ItemFilters {

    private ItemFilters() {}

    public static Predicate<Item> predicate(SearchCriteria c) {
        if (c == null) return i -> true;

        Predicate<Item> p = i -> true;

        if (c.nameQuery() != null) {
            p = p.and(i -> ItemListUtils.nameMatches(i.getName(), c.nameQuery(), c.nameMode()));
        }
        if (c.minQuantity() != null) {
            int min = c.minQuantity();
            p = p.and(i -> i.getQuantity() >= min);
        }
        if (c.maxQuantity() != null) {
            int max = c.maxQuantity();
            p = p.and(i -> i.getQuantity() <= max);
        }

        final LocalDate from = c.expirationFrom();
        final LocalDate until = c.expirationUntil();
        final boolean includeUnknown = c.includeUnknownExpiration();

        if (from != null) {
            p = p.and(i -> {
                LocalDate d = i.getExpirationDate();
                if (d == null) return includeUnknown;
                return !d.isBefore(from);
            });
        }
        if (until != null) {
            p = p.and(i -> {
                LocalDate d = i.getExpirationDate();
                if (d == null) return includeUnknown;
                return !d.isAfter(until);
            });
        }

        return p;
    }

    public static Comparator<Item> comparator(SearchCriteria c) {
        if (c == null || c.sort() == null || c.sort() == SearchSort.DEFAULT) {
            return ItemListUtils.ITEM_ORDER;
        }
        return switch (c.sort()) {
            case DEFAULT -> ItemListUtils.ITEM_ORDER;
            case NAME_ASC -> ItemListUtils.BY_NAME_ASC;
            case NAME_DESC -> ItemListUtils.BY_NAME_DESC;
            case EXPIRATION_ASC -> ItemListUtils.BY_EXPIRATION_ASC;
            case EXPIRATION_DESC -> ItemListUtils.BY_EXPIRATION_DESC;
            case QUANTITY_ASC -> ItemListUtils.BY_QUANTITY_ASC;
            case QUANTITY_DESC -> ItemListUtils.BY_QUANTITY_DESC;
        };
    }
}

