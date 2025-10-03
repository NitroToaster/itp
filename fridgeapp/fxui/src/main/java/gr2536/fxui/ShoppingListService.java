package gr2536.fxui;

import java.util.ArrayList;
import java.util.List;

import gr2536.fxui.ShoppingListController.ShoppingItem;

/**
 * Shared service to manage the ShoppingList data across navigation.
 * This allows the shopping list to persist when switching between interfaces.
 */
public class ShoppingListService {
    
    private static List<ShoppingItem> shoppingItems = new ArrayList<>();
    
    /**
     * Get the shared shopping list items.
     * @return the current list of shopping items
     */
    public static List<ShoppingItem> getItems() {
        return new ArrayList<>(shoppingItems);
    }
    
    /**
     * Set the shopping list items.
     * @param items the new list of shopping items
     */
    public static void setItems(List<ShoppingItem> items) {
        shoppingItems = new ArrayList<>(items);
    }
    
    /**
     * Add a single item to the shopping list.
     * @param item the item to add
     */
    public static void addItem(ShoppingItem item) {
        shoppingItems.add(item);
    }
    
    /**
     * Remove a single item from the shopping list.
     * @param item the item to remove
     */
    public static void removeItem(ShoppingItem item) {
        shoppingItems.remove(item);
    }
    
    /**
     * Clear all items from the shopping list.
     */
    public static void clear() {
        shoppingItems.clear();
    }
}
