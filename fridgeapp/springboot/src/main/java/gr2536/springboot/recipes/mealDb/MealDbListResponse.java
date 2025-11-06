package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Generic response wrapper for MealDB list endpoints (categories, areas). */
public record MealDbListResponse(@JsonProperty("meals") List<MealDbListValue> meals) {
  public MealDbListResponse {
    meals = meals == null ? List.of() : List.copyOf(meals);
  }
}

