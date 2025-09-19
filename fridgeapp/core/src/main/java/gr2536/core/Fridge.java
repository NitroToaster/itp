package gr2536.core;

// import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
// import gr2536.core.ItemListUtils;
// import gr2536.core.Key;
// import gr2536.core.Entry;

/**
 * A non-thread-safe fridge inventory that groups items by name,
 * storing them by expiration date.
 */
public class Fridge implements ItemList {

    private final Map<Key, Entry> inventoryByKey = new HashMap<>();

    /**
     * Adds an item, grouping by name and storing by expiration date.
     * Preserves the first-seen casing of the name.
     */
    public void add(Item item) {
        Objects.requireNonNull(item, "item");
        Key key = Key.of(item.getName());
        Entry entry = inventoryByKey.computeIfAbsent(key,
                k -> new Entry(item.getName().trim()));
        entry.quantitiesByExpiration.merge(item.getExpirationDate(), item.getQuantity(), ItemListUtils::clampAdd);
    }

    /**
     * Removes a quantity of an item, starting with those that expire soonest.
     * 
     * @return The remaining quantity of the item.
     */
    public int remove(String name, int quantityToRemove) {
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("quantityToRemove must be > 0");
        }
        Entry entry = inventoryByKey.get(Key.of(name));
        if (entry == null)
            return 0;

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
            inventoryByKey.remove(Key.of(name));
        }
        return ItemListUtils.clampToIntMax(remaining);
    }

    /**
     * Gets the total quantity of an item across all its expiration dates.
     */
    public int getQuantity(String name) {
        Entry entry = inventoryByKey.get(Key.of(name));
        if (entry == null)
            return 0;
        long total = entry.quantitiesByExpiration.values().stream().mapToLong(Integer::intValue).sum();
        return ItemListUtils.clampToIntMax(total);
    }

    /**
     * Returns a sorted, unmodifiable list of all items, with one entry per
     * expiration bucket.
     */
    public List<Item> listItems() {
        return inventoryByKey.values().stream()
                .flatMap(entry -> entry.quantitiesByExpiration.entrySet().stream()
                        .map(bucket -> new Item(entry.displayName, bucket.getValue(), bucket.getKey())))
                .sorted(ItemListUtils.ITEM_ORDER)
                .collect(Collectors.toUnmodifiableList());
    }
}