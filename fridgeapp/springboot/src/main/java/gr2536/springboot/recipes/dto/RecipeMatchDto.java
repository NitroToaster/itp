package gr2536.springboot.recipes.dto;

import java.util.List;

public record RecipeMatchDto(
        RecipeCard recipe,
        double matchRatio,
        int matchedIngredients,
        int totalIngredients,
        List<IngredientDto> missing
) {
  public RecipeMatchDto {
    missing = missing == null ? List.of() : List.copyOf(missing);
  }
}
