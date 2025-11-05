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
            String canonical = canonicalizeName(ing.getName());
            int required = ing.getQuantity();

            // Exact canonical match first
            int available = fridgeByName.getOrDefault(canonical, 0);

            // Try synonyms if exact not found
            if (available == 0) {
                for (String alt : synonymsOf(canonical)) {
                    available = fridgeByName.getOrDefault(alt, 0);
                    if (available > 0) break;
                }
            }

            // Count as matched only if fully satisfied (available >= required)
            if (available >= required && required > 0) {
                matched++;
            } else {
                int missingQty = Math.max(0, required - available);
                // Don't have this ingredient at all
                missing.add(new RecipeIngredient(ing.getName(), missingQty == 0 ? required : missingQty));
            }
        }

        return new RecipeMatch(recipe, missing, matched, total);
    }

    private Map<String, Integer> summarizeFridge(Collection<Item> fridgeItems) {
        Map<String, Integer> result = new HashMap<>();
        for (Item item : fridgeItems) {
            String key = canonicalizeName(item.getName());
            int current = result.getOrDefault(key, 0);
            int combined = ItemListUtils.clampAdd(current, item.getQuantity());
            result.put(key, combined);
        }
        return result;
    }

    private String canonicalizeName(String name) {
        if (name == null) return "";
        String s = name.toLowerCase(Locale.ROOT).trim();
        s = s.replace('-', ' ').replace('_', ' ');
        s = s.replaceAll("[^a-z0-9 ]", "");
        s = s.replaceAll("\\s+", " ");
        // simple plural normalization on last token
        String[] tokens = s.split(" ");
        if (tokens.length > 0) {
            String last = tokens[tokens.length - 1];
            if (last.length() > 3 && last.endsWith("s")) {
                tokens[tokens.length - 1] = last.substring(0, last.length() - 1);
            }
        }
        String canon = String.join(" ", tokens).trim();
        // map common synonyms to a unified canonical form
        switch (canon) {
            case "spring onions":
            case "spring onion":
            case "green onion":
            case "green onions":
            case "scallion":
            case "scallions":
                return "spring onion";
            case "bell pepper":
            case "capsicum":
                return "bell pepper";
            case "chilli":
                return "chili";
            default:
                return canon;
        }
    }

    private List<String> synonymsOf(String canonical) {
        switch (canonical) {
            case "spring onion":
                return Arrays.asList("green onion", "scallion");
            case "bell pepper":
                return Arrays.asList("capsicum", "green pepper", "red pepper");
            case "chili":
                return Collections.singletonList("chilli");
            // Protein families (map cuts/ground to base and vice versa)
            case "chicken":
                return Arrays.asList(
                    "chicken breast", "chicken thigh", "chicken wings", "chicken wing",
                    "chicken drumstick", "chicken leg", "chicken fillet"
                );
            case "chicken breast":
            case "chicken thigh":
            case "chicken wings":
            case "chicken wing":
            case "chicken drumstick":
            case "chicken leg":
            case "chicken fillet":
                return Collections.singletonList("chicken");

            case "beef":
                return Arrays.asList(
                    "ground beef", "beef mince", "minced beef", "beef sirloin", "beef roast", "beef steak"
                );
            case "ground beef":
            case "beef mince":
            case "minced beef":
            case "beef sirloin":
            case "beef roast":
            case "beef steak":
                return Collections.singletonList("beef");

            case "pork":
                return Arrays.asList(
                    "pork loin", "pork belly", "pork chop", "pork chops", "pork shoulder", "ground pork", "minced pork"
                );
            case "pork loin":
            case "pork belly":
            case "pork chop":
            case "pork chops":
            case "pork shoulder":
            case "ground pork":
            case "minced pork":
                return Collections.singletonList("pork");

            case "lamb":
                return Arrays.asList("lamb mince", "minced lamb", "lamb rack", "lamb chop", "lamb chops");
            case "lamb mince":
            case "minced lamb":
            case "lamb rack":
            case "lamb chop":
            case "lamb chops":
                return Collections.singletonList("lamb");
            default:
                return Collections.emptyList();
        }
    }

    // Staples are currently included in scoring to keep UI and counts aligned
}
