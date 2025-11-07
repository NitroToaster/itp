package gr2536.springboot.recipes.dto;

import java.util.List;

public record RecipeDetails(
    String id,
    String title,
    String image,
    String category,
    String area,
    List<IngredientDto> ingredients,
    String instructions,
    String youtubeUrl
) {
  public RecipeDetails {
    ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
  }
}
