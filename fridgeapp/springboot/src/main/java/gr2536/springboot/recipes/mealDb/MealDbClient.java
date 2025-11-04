package gr2536.springboot.recipes.mealDb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MealDbClient {
  private final RestTemplate http;
  private final String baseUrl;

  public MealDbClient(
      RestTemplate http,
      @Value("${recipes.mealdb.base-url:https://www.themealdb.com/api/json/v1/1}") String baseUrl
  ) {
    this.http = http;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  public MealDbSearchResponse searchByName(String q) {
    String url = baseUrl + "/search.php?s={q}";
    return http.getForObject(url, MealDbSearchResponse.class, q);
  }

  public MealDbSearchResponse lookupById(String id) {
    String url = baseUrl + "/lookup.php?i={id}";
    return http.getForObject(url, MealDbSearchResponse.class, id);
  }

  public MealDbSearchResponse filterByIngredient(String ingredient) {
    // TheMealDB expects underscores instead of spaces for ingredient names
    String normalized = ingredient == null ? "" : ingredient.trim().replace(' ', '_');
    String url = baseUrl + "/filter.php?i={ing}";
    return http.getForObject(url, MealDbSearchResponse.class, normalized);
  }

  public MealDbListResponse listCategories() {
    String url = baseUrl + "/list.php?c=list";
    return http.getForObject(url, MealDbListResponse.class);
  }

  public MealDbListResponse listAreas() {
    String url = baseUrl + "/list.php?a=list";
    return http.getForObject(url, MealDbListResponse.class);
  }

  /** Filter by MealDB category (e.g., "Seafood"). */
  public MealDbSearchResponse filterByCategory(String category) {
    String url = baseUrl + "/filter.php?c={cat}";
    return http.getForObject(url, MealDbSearchResponse.class, category);
  }

  /** Filter by MealDB area (e.g., "Canadian"). */
  public MealDbSearchResponse filterByArea(String area) {
    String url = baseUrl + "/filter.php?a={area}";
    return http.getForObject(url, MealDbSearchResponse.class, area);
  }
}
