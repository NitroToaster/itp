package gr2536.core;

import java.time.LocalDate;
import java.util.Objects;

import java.util.Locale;

/**
 * An immutable inventory item.
 * <p>
 * Name and unit are trimmed on creation, but original casing is preserved.
 * Equality is case-insensitive on name and unit, and also considers quantity
 * and expiration date. For grouping, normalize name and unit to lowercase.
 */
public final class Item {

    private final String name;
    private final int quantity;
    private final LocalDate expirationDate; // nullable when unknown

    /**
     * Creates a new immutable item.
     *
     * @param name           item name (must be non-blank)
     * @param quantity       item quantity (must be > 0)
     * @param expirationDate expiration date (nullable)
     * @throws IllegalArgumentException when validation fails
     */
    public Item(String name, int quantity, LocalDate expirationDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must be non-empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.name = name.trim();
        this.quantity = quantity;
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Returns a new {@code Item} with the provided quantity, keeping other fields the same.
     */
    public Item withQuantity(int newQuantity) {
        return new Item(this.name, newQuantity, this.expirationDate);
    }

    /**
     * Returns a new {@code Item} with the provided expiration date, keeping other fields the same.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return quantity == item.quantity
                && name.equalsIgnoreCase(item.name)
                && Objects.equals(expirationDate, item.expirationDate);
    }

    /**
     * Returns a uniqe hash code consistent with an Item object
     */
    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(Locale.ROOT), quantity, expirationDate);
    }

    /**
     * Returns a string representation of the item.
     */
    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", expirationDate=" + expirationDate +
                '}';
    }
}


