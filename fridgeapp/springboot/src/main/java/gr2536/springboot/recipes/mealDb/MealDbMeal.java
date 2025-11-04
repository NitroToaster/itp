package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MealDbMeal(
    @JsonProperty("idMeal") String idMeal,
    @JsonProperty("strMeal") String strMeal,
    @JsonProperty("strMealThumb") String strMealThumb,
    @JsonProperty("strCategory") String strCategory,
    @JsonProperty("strArea") String strArea,
    @JsonProperty("strInstructions") String strInstructions,
    @JsonProperty("strYoutube") String strYoutube,

    @JsonProperty("strIngredient1") String strIngredient1,
    @JsonProperty("strMeasure1") String strMeasure1
) {}
