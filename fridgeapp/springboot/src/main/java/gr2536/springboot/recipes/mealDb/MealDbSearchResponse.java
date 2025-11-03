package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MealDbSearchResponse(@JsonProperty("meals") List<MealDbMeal> meals) {}