package gr2536.springboot.recipes.dto;

/**
 * Card DTO with match metadata for ingredient-based filtering.
 */
public record RecipeCardMatch(String id, String title, String image, int matched, int total) {}


