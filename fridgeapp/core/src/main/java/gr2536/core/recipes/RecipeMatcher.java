package gr2536.core.recipes;

import gr2536.core.item.Item;
import gr2536.core.utils.ItemListUtils;

import java.util.*;
import java.util.stream.Collectors;

public final class RecipeMatcher {

    public List<RecipeMatch> findMatches(Collection<Recipe> recipes,
                                         Collection<Item> fridgeItems) {

        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(fridgeItems, "fridgeItems");

        Map<String, Integer> fridgeByName = summarizeFridge(fridgeItems);

        List<RecipeMatch> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            result.add(matchRecipe(recipe, fridgeByName));
        }

        return result.stream()
                .sorted(Comparator
                        .comparingDouble(RecipeMatch::getMatchRatio).reversed()
                        .thenComparing(r -> r.getRecipe().getName(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<RecipeIngredient> computeMissingIngredients(Recipe recipe,
                                                            Collection<Item> fridgeItems) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(fridgeItems, "fridgeItems");
        Map<String, Integer> fridgeByName = summarizeFridge(fridgeItems);
        return matchRecipe(recipe, fridgeByName).getMissingIngredients();
    }

    public List<Item> computeMissingAsItems(Recipe recipe,
                                            Collection<Item> fridgeItems) {
        List<RecipeIngredient> missing = computeMissingIngredients(recipe, fridgeItems);
        List<Item> items = new ArrayList<>();
        for (RecipeIngredient ing : missing) {
            items.add(new Item(ing.getName(), ing.getQuantity(), null));
        }
        return List.copyOf(items);
    }

    private RecipeMatch matchRecipe(Recipe recipe, Map<String, Integer> fridgeByName) {

        List<RecipeIngredient> missing = new ArrayList<>();
        int matched = 0;
        int total = recipe.getIngredients().size();

        for (RecipeIngredient ing : recipe.getIngredients()) {
            String normalized = normalizeName(ing.getName());
            int required = ing.getQuantity();
            int available = fridgeByName.getOrDefault(normalized, 0);

            if (available >= required) {
                matched++;
            } else {
                int missingQty = required - available;
                if (missingQty > 0) {
                    missing.add(new RecipeIngredient(ing.getName(), missingQty));
                }
            }
        }

        return new RecipeMatch(recipe, missing, matched, total);
    }

    private Map<String, Integer> summarizeFridge(Collection<Item> fridgeItems) {
        Map<String, Integer> result = new HashMap<>();
        for (Item item : fridgeItems) {
            String key = normalizeName(item.getName());
            int current = result.getOrDefault(key, 0);
            int combined = ItemListUtils.clampAdd(current, item.getQuantity());
            result.put(key, combined);
        }
        return result;
    }

    private String normalizeName(String name) {
        return (name == null ? "" : name.trim().toLowerCase(Locale.ROOT));
    }
}
