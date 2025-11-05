package gr2536.core.recipes;

import gr2536.core.item.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeMatcherTest {

    @Test
    void doesNotMatchGarlicPowderWhenOnlyGarlicInFridge() {
        // Fridge has fresh garlic only
        List<Item> fridge = List.of(new Item("Garlic", 1, null));

        // Recipe requires garlic powder (should not match garlic)
        Recipe recipe = new Recipe(
            "r1",
            "Test Recipe",
            null,
            List.of(new RecipeIngredient("Garlic powder", 1)),
            null
        );

        RecipeMatcher matcher = new RecipeMatcher();
        var matches = matcher.findMatches(List.of(recipe), fridge);
        var match = matches.get(0);

        assertEquals(0, match.getMatchedIngredients(), "garlic powder must not match garlic");
        assertEquals(1, match.getTotalIngredients());
    }

    @Test
    void matchesSpringOnionWithGreenOnionSynonym() {
        // Fridge has green onion
        List<Item> fridge = List.of(new Item("Green Onions", 2, null));

        // Recipe requires spring onions
        Recipe recipe = new Recipe(
            "r2",
            "Test Recipe 2",
            null,
            List.of(new RecipeIngredient("Spring onions", 1)),
            null
        );

        RecipeMatcher matcher = new RecipeMatcher();
        var matches = matcher.findMatches(List.of(recipe), fridge);
        var match = matches.get(0);

        assertEquals(1, match.getMatchedIngredients(), "spring onion should match green onion");
        assertEquals(1, match.getTotalIngredients());
    }
}


