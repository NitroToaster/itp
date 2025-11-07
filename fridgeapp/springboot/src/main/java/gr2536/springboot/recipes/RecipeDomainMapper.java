package gr2536.springboot.recipes;

import gr2536.core.recipes.Recipe;
import gr2536.core.recipes.RecipeIngredient;
import gr2536.springboot.recipes.dto.IngredientDto;
import gr2536.springboot.recipes.dto.RecipeDetails;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RecipeDomainMapper {

    private static final Pattern LEADING_INT = Pattern.compile("^\\s*(\\d+)");

    private RecipeDomainMapper() {}

    static Recipe toCoreRecipe(RecipeDetails dto) {
        Map<String, RecipeIngredient> unique = new LinkedHashMap<>();
        if (dto.ingredients() != null) {
            for (IngredientDto ing : dto.ingredients()) {
                if (ing == null || ing.name() == null || ing.name().isBlank()) {
                  continue;
                }

                String trimmedName = ing.name().trim();
                int qty = Math.max(1, parseQuantity(ing.measure()));
                String key = trimmedName.toLowerCase(Locale.ROOT);

                unique.merge(key,
                    new RecipeIngredient(trimmedName, qty),
                    (existing, incoming) -> existing.withQuantity(existing.getQuantity() + incoming.getQuantity()));
            }
        }
        List<RecipeIngredient> ings = new ArrayList<>(unique.values());
        String img = dto.image();
        String instr = dto.instructions();
        return new Recipe(dto.id(), dto.title(), (img == null || img.isBlank() ? null : img), ings, instr);
    }

    private static int parseQuantity(String measure) {
        if (measure == null) {
          return 1;
        }
        Matcher m = LEADING_INT.matcher(measure);
        if (m.find()) {
            try {
              return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignore) {
              return 1;
            }
        }
        return 1;
    }
}
