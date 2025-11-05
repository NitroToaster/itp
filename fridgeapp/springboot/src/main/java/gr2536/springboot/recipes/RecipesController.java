package gr2536.springboot.recipes;

import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeDetails;
import gr2536.springboot.recipes.dto.RecipeCardMatch;
import gr2536.springboot.recipes.dto.RecipeMeta;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipesController {

  private final RecipeService svc;

  @GetMapping("/search")
  public List<RecipeCard> search(@RequestParam String q) {
    return svc.search(q);
  }

  @GetMapping("/{id}")
  public RecipeDetails one(@PathVariable String id) {
    var dto = svc.byId(id);
    if (dto == null) throw new RecipeNotFound(id);
    return dto;
  }

  @GetMapping("/filter")
  public List<RecipeCard> filter(
      @RequestParam(name = "i") List<String> ingredients,
      @RequestParam(name = "match", defaultValue = "all") String match
  ) {
    boolean matchAll = !"any".equalsIgnoreCase(match);
    return svc.filterByIngredients(ingredients, matchAll);
  }

  /**
   * Advanced filter that supports ingredient chips (any/all), category, area and name.
   * Results are verified against full recipe details and scored by matched count.
   */
  @GetMapping("/filter2")
  public List<RecipeCardMatch> filter2(
      @RequestParam(name = "i", required = false) List<String> ingredients,
      @RequestParam(name = "match", defaultValue = "all") String match,
      @RequestParam(name = "minMatched", defaultValue = "1") int minMatched,
      @RequestParam(name = "category", required = false) String category,
      @RequestParam(name = "area", required = false) String area,
      @RequestParam(name = "q", required = false) String name,
      @RequestParam(name = "limit", defaultValue = "30") int limit,
      @RequestParam(name = "offset", defaultValue = "0") int offset
  ) {
    boolean matchAll = !"any".equalsIgnoreCase(match);
    return svc.filterWithVerify(ingredients, matchAll, minMatched, category, area, name, limit, offset);
  }

  /** Returns available categories and areas for driving filter dropdowns. */
  @GetMapping("/meta")
  public RecipeMeta meta() {
    return svc.meta();
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RecipeNotFound.class)
  void notFound() {}

  static class RecipeNotFound extends RuntimeException {
    RecipeNotFound(String id) { super("Recipe not found: " + id); }
  }
}
