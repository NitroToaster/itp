package gr2536.fxui;

import java.util.List;

public final class RecipeModels {
    private RecipeModels() {}

    public record RecipeCard(String id, String title, String image) {}

    public record RecipeCardMatch(String id, String title, String image, int matched, int total) {}

    public record IngredientDto(String name, String measure) {}

    public record RecipeDetails(
        String id,
        String title,
        String image,
        String category,
        String area,
        List<IngredientDto> ingredients,
        String instructions,
        String youtubeUrl
    ) {}

    public record RecipeMeta(List<String> categories, List<String> areas) {}
}


