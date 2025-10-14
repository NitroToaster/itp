// Used Github Copilot to refactor code from Fridge

package gr2536.core;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for shared static methods
 */
class ItemListUtils {
    static int clampToIntMax(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    static int clampAdd(int a, int b) {
        return clampToIntMax((long) a + b);
    }

    public static final Comparator<Item> ITEM_ORDER = Comparator
            .comparing((Item i) -> i.getName().toLowerCase(Locale.ROOT))
            .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    // Additional comparators used by search sorting
    public static final Comparator<Item> BY_NAME_ASC = Comparator
            .comparing((Item i) -> i.getName().toLowerCase(Locale.ROOT))
            .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Item> BY_NAME_DESC = Comparator
            .comparing((Item i) -> i.getName().toLowerCase(Locale.ROOT), Comparator.reverseOrder())
            .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Item> BY_EXPIRATION_ASC = Comparator
            .comparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(i -> i.getName().toLowerCase(Locale.ROOT));

    public static final Comparator<Item> BY_EXPIRATION_DESC = Comparator
            .comparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(i -> i.getName().toLowerCase(Locale.ROOT));

    public static final Comparator<Item> BY_QUANTITY_ASC = Comparator
            .comparingInt(Item::getQuantity)
            .thenComparing(i -> i.getName().toLowerCase(Locale.ROOT))
            .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    public static final Comparator<Item> BY_QUANTITY_DESC = Comparator
            .comparingInt(Item::getQuantity).reversed()
            .thenComparing(i -> i.getName().toLowerCase(Locale.ROOT))
            .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    static boolean nameMatches(String name, String query, NameMatchMode mode) {
        if (query == null) return true;
        String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return true;
        NameMatchMode m = (mode == null) ? NameMatchMode.CONTAINS : mode;
        return switch (m) {
            case EXACT -> n.equals(q);
            case PREFIX -> n.startsWith(q);
            case CONTAINS -> n.contains(q);
        };
    }

    /**
     * Checks if a name matches a query using all match modes (CONTAINS, PREFIX, EXACT).
     * Returns true if the name matches using any of the three modes.
     */
    static boolean nameMatchesAll(String name, String query) {
        if (query == null) return true;
        String n = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return true;
        
        // Try all three match modes
        return n.equals(q) || n.startsWith(q) || n.contains(q);
    }
}

// Package-private record for grouping items by name
record Key(String name) {
    static Key of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must be non-empty");
        }
        return new Key(name.trim().toLowerCase(Locale.ROOT));
    }
}

// Package-private class for storing item quantities by expiration date
class Entry {
    final String displayName;

    final Map<LocalDate, Integer> quantitiesByExpiration = new TreeMap<>(
            Comparator.nullsLast(Comparator.naturalOrder()));

    Entry(String displayName) {
        this.displayName = displayName;
    }
}
