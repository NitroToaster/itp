package gr2536.springboot.recipes;

import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeDetails;
import gr2536.springboot.recipes.mealDb.MealDbClient;
import gr2536.springboot.recipes.mealDb.MealDbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
  private final MealDbClient client;

  public List<RecipeCard> search(String q) {
    var resp = client.searchByName(q);
    return MealDbMapper.toCards(resp);
  }

  public RecipeDetails byId(String id) {
    var resp = client.lookupById(id);
    if (resp == null || resp.meals() == null || resp.meals().isEmpty()) return null;
    return MealDbMapper.toDetail(resp.meals().get(0));
  }
}
