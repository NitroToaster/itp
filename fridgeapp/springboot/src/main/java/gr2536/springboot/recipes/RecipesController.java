package gr2536.springboot.recipes;

import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeDetails;
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

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RecipeNotFound.class)
  void notFound() {}

  static class RecipeNotFound extends RuntimeException {
    RecipeNotFound(String id) { super("Recipe not found: " + id); }
  }
}
