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
