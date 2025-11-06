package gr2536.core.recipes;

import java.util.Locale;
import java.util.Objects;

public final class RecipeIngredient {

    private final String name;
    private final int quantity; 
    
    public RecipeIngredient(String name, int quantity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must be non-empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        this.name = name.trim();
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public RecipeIngredient withQuantity(int newQuantity) {
        return new RecipeIngredient(this.name, newQuantity);
    }

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredient that)) return false;
        return quantity == that.quantity &&
                name.equalsIgnoreCase(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(Locale.ROOT), quantity);
    }
}
