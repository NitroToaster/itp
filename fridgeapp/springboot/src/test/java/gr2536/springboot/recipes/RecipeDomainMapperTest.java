package gr2536.springboot.recipes;

import gr2536.core.recipes.Recipe;
import gr2536.core.recipes.RecipeIngredient;
import gr2536.springboot.recipes.dto.IngredientDto;
import gr2536.springboot.recipes.dto.RecipeDetails;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipeDomainMapperTest {

    @Test
    void toCoreRecipe_deduplicatesIngredientsByNameIgnoringCase() {
        RecipeDetails dto = new RecipeDetails(
            "id-1",
            "Test Recipe",
            null,
            null,
            null,
            List.of(
                new IngredientDto("Garlic", "2 cloves"),
                new IngredientDto(" garlic ", "1"),
                new IngredientDto("Spring Onions", null),
                new IngredientDto("Onion", null),
                new IngredientDto("Onion", null)
            ),
            null,
            null
        );

        Recipe recipe = RecipeDomainMapper.toCoreRecipe(dto);
        List<RecipeIngredient> ingredients = recipe.getIngredients();

        assertEquals(3, ingredients.size(), "Duplicate ingredient names should be merged");
        assertEquals("Garlic", ingredients.get(0).getName());
        assertEquals(3, ingredients.get(0).getQuantity(), "Quantities for duplicate ingredients should be summed");
        assertEquals("Spring Onions", ingredients.get(1).getName());
        assertEquals("Onion", ingredients.get(2).getName());
    }
}

