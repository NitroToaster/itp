package gr2536.core;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A non-thread-safe fridge inventory that groups items by name and unit,
 * storing them by expiration date.
 */
public class Fridge {

    private final Map<Key, Entry> inventoryByKey = new HashMap<>();

    /**
     * Adds an item, grouping by name/unit and storing by expiration date.
     * Preserves the first-seen casing of the name and unit.
     */
    public void add(Item item) {
        Objects.requireNonNull(item, "item");
        Key key = Key.of(item.getName(), item.getUnit());
        Entry entry = inventoryByKey.computeIfAbsent(key,
            k -> new Entry(item.getName().trim(), item.getUnit().trim()));
        entry.quantitiesByExpiration.merge(item.getExpirationDate(), item.getQuantity(), Fridge::clampAdd);
    }

    /**
     * Removes a quantity of an item, starting with those that expire soonest.
     * @return The remaining quantity of the item.
     */
    public int remove(String name, String unit, int quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("quantityToRemove must be > 0");
        }
        Entry entry = inventoryByKey.get(Key.of(name, unit));
        if (entry == null) return 0;

        int toRemove = quantityToRemove;
        var it = entry.quantitiesByExpiration.entrySet().iterator();
        while (toRemove > 0 && it.hasNext()) {
            var e = it.next();
            int used = Math.min(e.getValue(), toRemove);
            if (e.getValue() - used == 0) {
                it.remove();
            } else {
                e.setValue(e.getValue() - used);
            }
            toRemove -= used;
        }

        long remaining = entry.quantitiesByExpiration.values().stream().mapToLong(Integer::intValue).sum();
        if (remaining == 0) {
            inventoryByKey.remove(Key.of(name, unit));
        }
        return clampToIntMax(remaining);
    }

    /**
     * Gets the total quantity of an item across all its expiration dates.
     */
    public int getQuantity(String name, String unit) {
        Entry entry = inventoryByKey.get(Key.of(name, unit));
        if (entry == null) return 0;
        long total = entry.quantitiesByExpiration.values().stream().mapToLong(Integer::intValue).sum();
        return clampToIntMax(total);
    }

    private static final Comparator<Item> ITEM_ORDER =
            Comparator.comparing((Item i) -> i.getName().toLowerCase(Locale.ROOT))
                      .thenComparing(i -> i.getUnit().toLowerCase(Locale.ROOT))
                      .thenComparing(Item::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder()));

    /**
     * Returns a sorted, unmodifiable list of all items, with one entry per expiration bucket.
     */
    public List<Item> listItems() {
        return inventoryByKey.values().stream()
                .flatMap(entry -> entry.quantitiesByExpiration.entrySet().stream()
                    .map(bucket -> new Item(entry.displayName, bucket.getValue(), entry.displayUnit, bucket.getKey())))
                .sorted(ITEM_ORDER)
                .collect(Collectors.toUnmodifiableList());
    }

    private record Key(String name, String unit) {
        static Key of(String name, String unit) {
            if (name == null || name.isBlank() || unit == null || unit.isBlank()) {
                throw new IllegalArgumentException("Name and unit must be non-empty");
            }
            return new Key(name.trim().toLowerCase(Locale.ROOT), unit.trim().toLowerCase(Locale.ROOT));
        }
    }

    private static class Entry {
        final String displayName;
        final String displayUnit;
        final Map<LocalDate, Integer> quantitiesByExpiration = new TreeMap<>(Comparator.nullsLast(Comparator.naturalOrder()));

        Entry(String displayName, String displayUnit) {
            this.displayName = displayName;
            this.displayUnit = displayUnit;
        }
    }

    private static int clampToIntMax(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static int clampAdd(int a, int b) {
        return clampToIntMax((long) a + b);
    }
}