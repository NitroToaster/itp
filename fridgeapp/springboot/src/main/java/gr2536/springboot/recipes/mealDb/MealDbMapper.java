package gr2536.springboot.recipes.mealDb;

import gr2536.springboot.recipes.dto.IngredientDto;
import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeDetails;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class MealDbMapper {
  private MealDbMapper(){}

  public static List<RecipeCard> toCards(MealDbSearchResponse r) {
    if (r == null) return List.of();
    var meals = r.meals();
    if (meals.isEmpty()) return List.of();
    return meals.stream()
        .map(m -> new RecipeCard(m.idMeal(), m.strMeal(), m.strMealThumb()))
        .toList();
  }

  public static RecipeDetails toDetail(MealDbMeal m) {
    return new RecipeDetails(
        m.idMeal(),
        m.strMeal(),
        m.strMealThumb(),
        m.strCategory(),
        m.strArea(),
        extractIngredients(m),
        m.strInstructions(),
        m.strYoutube()
    );
  }

  public static List<IngredientDto> extractIngredients(MealDbMeal m) {
    var list = new ArrayList<IngredientDto>();
    try {
      Class<?> c = m.getClass();
      for (int i = 1; i <= 20; i++) {
        Method gi = c.getMethod("strIngredient" + i);
        Method gm = c.getMethod("strMeasure" + i);
        String name = (String) gi.invoke(m);
        String measure = (String) gm.invoke(m);
        if (name != null && !name.isBlank()) {
          list.add(new IngredientDto(name.trim(), measure == null ? "" : measure.trim()));
        }
      }
    } catch (ReflectiveOperationException ignored) {}
    return List.copyOf(list);
  }
}
