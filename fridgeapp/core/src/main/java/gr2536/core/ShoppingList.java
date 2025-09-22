package gr2536.core;

import java.time.LocalDate;
import java.util.*;
// import java.util.stream.Collectors;
import java.util.stream.Collectors;

public class ShoppingList implements ItemList {

    private final Map<Key, Entry> shoppingListByKey = new HashMap<>();

    /**
     * Adds an item, grouping by name and storing by expiration date.
     * Preserves the first-seen casing of the name and.
     */
    public void add(Item item) {
        Objects.requireNonNull(item, "item");
        Key key = Key.of(item.getName());
        Entry entry = shoppingListByKey.computeIfAbsent(key,
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
        Entry entry = shoppingListByKey.get(Key.of(name));
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
            shoppingListByKey.remove(Key.of(name));
        }
        return ItemListUtils.clampToIntMax(remaining);
    }

    /**
     * Gets the total quantity of an item across all its expiration dates.
     */
    public int getQuantity(String name) {
        Entry entry = shoppingListByKey.get(Key.of(name));
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
        return shoppingListByKey.values().stream()
                .flatMap(entry -> entry.quantitiesByExpiration.entrySet().stream()
                        .map(bucket -> new Item(entry.displayName, bucket.getValue(), bucket.getKey())))
                .sorted(ItemListUtils.ITEM_ORDER)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Adds all items from shopping list to fridge
     * Removes all items from shopping list
     * 
     * @param fridge
     */
    public void addToFridge(Fridge fridge) {
        List<Item> list = listItems();
        var it = list.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            fridge.add(item);
        }
        shoppingListByKey.clear();
    }

    /**
     * Searches items in the shopping list using the provided criteria.
     * When {@code criteria} is null, returns all items sorted by the default order.
     */
    public List<Item> search(SearchCriteria criteria) {
        SearchCriteria c = (criteria == null) ? SearchCriteria.of() : criteria;
        return listItems().stream()
                .filter(ItemFilters.predicate(c))
                .sorted(ItemFilters.comparator(c))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Item> filterByQuantityAtLeast(int min) {
        if (min < 0) {
            throw new IllegalArgumentException("min must be >= 0");
        }
        return search(new SearchCriteria(null, NameMatchMode.CONTAINS, Integer.valueOf(min), null, null, null, true, SearchSort.DEFAULT));
    }

    public List<Item> filterByQuantityAtMost(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max must be >= 0");
        }
        return search(new SearchCriteria(null, NameMatchMode.CONTAINS, null, Integer.valueOf(max), null, null, true, SearchSort.DEFAULT));
    }

    public List<Item> filterByExpirationFrom(LocalDate from) {
        Objects.requireNonNull(from, "from");
        return search(new SearchCriteria(null, NameMatchMode.CONTAINS, null, null, from, null, true, SearchSort.DEFAULT));
    }

    public List<Item> filterByExpirationUntil(LocalDate until) {
        Objects.requireNonNull(until, "until");
        return search(new SearchCriteria(null, NameMatchMode.CONTAINS, null, null, null, until, true, SearchSort.DEFAULT));
    }
}