package gr2536.springboot.recipes.mealDb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class MealDbSearchResponse {
    
    @JsonProperty("meals")
    private List<MealDbMeal> meals;
    
    public List<MealDbMeal> meals() {
        return meals != null ? meals : new ArrayList<>();
    }
    
    public void setMeals(List<MealDbMeal> meals) {
        this.meals = meals;
    }
    
    public MealDbSearchResponse() {}
}