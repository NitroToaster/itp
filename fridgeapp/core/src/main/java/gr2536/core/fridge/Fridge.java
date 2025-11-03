package gr2536.core.fridge;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import gr2536.core.item.Item;
import gr2536.core.item.ItemList;
import gr2536.core.utils.Entry;
import gr2536.core.utils.ItemFilters;
import gr2536.core.utils.ItemListUtils;
import gr2536.core.utils.Key;
import gr2536.core.utils.NameMatchMode;
import gr2536.core.utils.SearchCriteria;
import gr2536.core.utils.SearchSort;

/**
 * A non-thread-safe fridge inventory that groups items by name,
 * storing them by expiration date.
 */
public class Fridge implements ItemList {

    private final Map<Key, Entry> inventoryByKey = new HashMap<>();

    private FridgeFileManager fileManager;
    private String filename;

    /**
     * Creates an empty fridge without persistence.
     */
    public Fridge() {
    }

    /**
     * Creates a fridge with the specified file manager and filename for
     * persistence.
     * 
     * @param fileManager The file manager to use.
     * @param filename    The filename to use.
     */
    public Fridge(FridgeFileManager fileManager, String filename) {
        this.fileManager = fileManager;
        this.filename = filename;
    }

    /**
     * Sets the file manager and filename for persistence.
     * 
     * @param fileManager The file manager to use.
     * @param filename    The filename to use.
     */
    public void setFileManager(FridgeFileManager fileManager, String filename) {
        this.fileManager = fileManager;
        this.filename = filename;
    }

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

        save();
    }

    private void save() {
        if (fileManager != null && filename != null) {
            fileManager.saveFridgeData(this, filename);
        }
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

        save();
        
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

    /**
     * Searches items using the provided criteria. When {@code criteria} is null,
     * returns all items sorted by the default order.
     */
    public List<Item> search(SearchCriteria criteria) {
        SearchCriteria c = (criteria == null) ? SearchCriteria.of() : criteria;
        return listItems().stream()
                .filter(ItemFilters.predicate(c))
                .sorted(ItemFilters.comparator(c))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Searches items using all match modes (CONTAINS, PREFIX, EXACT) for the name query.
     * This method creates a custom search that matches items using any of the three modes.
     */
    public List<Item> searchWithAllModes(String nameQuery, Integer minQuantity, Integer maxQuantity, 
                                       LocalDate expirationFrom, LocalDate expirationUntil, 
                                       boolean includeUnknownExpiration, SearchSort sort) {
        SearchCriteria c = new SearchCriteria(nameQuery, null, minQuantity, maxQuantity, 
                                            expirationFrom, expirationUntil, includeUnknownExpiration, sort);
        return listItems().stream()
                .filter(item -> {
                    // Custom name matching using all modes
                    if (nameQuery != null && !nameQuery.trim().isEmpty()) {
                        if (!ItemListUtils.nameMatchesAll(item.getName(), nameQuery)) {
                            return false;
                        }
                    }
                    
                    // Apply other filters
                    if (minQuantity != null && item.getQuantity() < minQuantity) {
                        return false;
                    }
                    if (maxQuantity != null && item.getQuantity() > maxQuantity) {
                        return false;
                    }
                    
                    LocalDate itemExpiration = item.getExpirationDate();
                    if (expirationFrom != null) {
                        if (itemExpiration == null && !includeUnknownExpiration) {
                            return false;
                        }
                        if (itemExpiration != null && itemExpiration.isBefore(expirationFrom)) {
                            return false;
                        }
                    }
                    if (expirationUntil != null) {
                        if (itemExpiration == null && !includeUnknownExpiration) {
                            return false;
                        }
                        if (itemExpiration != null && itemExpiration.isAfter(expirationUntil)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
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

    @JsonGetter("items")
    public List<Item> getItemsForSerialization() {
        return new ArrayList<>(listItems());
    }
    
    @JsonSetter("items")
    public void setItemsFromSerialization(List<Item> items) {
        // Temporarily disable fileManager during deserialization
        FridgeFileManager tempFileManager = this.fileManager;
        String tempFilename = this.filename;
        this.fileManager = null;
        this.filename = null;
        
        for (Item item : items) {
            add(item);
        }
        
        // Restore fileManager
        this.fileManager = tempFileManager;
        this.filename = tempFilename;
    }
}