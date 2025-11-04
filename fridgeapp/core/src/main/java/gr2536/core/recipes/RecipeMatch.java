package gr2536.core.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RecipeMatch {

    private final Recipe recipe;
    private final List<RecipeIngredient> missingIngredients;
    private final int matchedIngredients;
    private final int totalIngredients;

    public RecipeMatch(Recipe recipe,
                       List<RecipeIngredient> missingIngredients,
                       int matchedIngredients,
                       int totalIngredients) {

        this.recipe = Objects.requireNonNull(recipe, "recipe");
        this.totalIngredients = totalIngredients;

        if (totalIngredients <= 0) {
            throw new IllegalArgumentException("totalIngredients must be > 0");
        }
        if (matchedIngredients < 0 || matchedIngredients > totalIngredients) {
            throw new IllegalArgumentException("matchedIngredients out of range");
        }

        this.matchedIngredients = matchedIngredients;
        this.missingIngredients = Collections.unmodifiableList(
                new ArrayList<>(Objects.requireNonNull(missingIngredients, "missingIngredients")));
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<RecipeIngredient> getMissingIngredients() {
        return missingIngredients;
    }

    public int getMatchedIngredients() {
        return matchedIngredients;
    }

    public int getTotalIngredients() {
        return totalIngredients;
    }

    public double getMatchRatio() {
        return (double) matchedIngredients / (double) totalIngredients;
    }
}
