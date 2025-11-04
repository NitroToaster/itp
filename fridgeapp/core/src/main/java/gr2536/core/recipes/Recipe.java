package gr2536.core.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Recipe {

    private final String id;
    private final String name;
    private final String imageUrl;
    private final List<RecipeIngredient> ingredients;
    private final String instructions;

    public Recipe(String id,
                  String name,
                  String imageUrl,
                  List<RecipeIngredient> ingredients,
                  String instructions) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must be non-empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must be non-empty");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("ingredients must not be empty");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.imageUrl = (imageUrl == null || imageUrl.isBlank()) ? null : imageUrl.trim();
        this.instructions = (instructions == null || instructions.isBlank()) ? null : instructions.trim();

        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ingredients=" + ingredients.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe recipe)) return false;
        // equality by id is usually enough
        return id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
