package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MealDbSearchResponse {
    
    @JsonProperty("meals")
    private List<MealDbMeal> meals = List.of();
    
    public List<MealDbMeal> meals() {
        return List.copyOf(meals);
    }
    
    public void setMeals(List<MealDbMeal> meals) {
        this.meals = meals == null ? List.of() : List.copyOf(meals);
    }
    
    public MealDbSearchResponse() {}
}
