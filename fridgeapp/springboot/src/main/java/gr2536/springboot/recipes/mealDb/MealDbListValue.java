package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Item shape for MealDB list endpoints. Only one of the fields will be non-null per response. */
public record MealDbListValue(
    @JsonProperty("strCategory") String strCategory,
    @JsonProperty("strArea") String strArea
) {}


