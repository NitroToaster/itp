package gr2536.fxui;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class RecipeService {
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final boolean directMealDbMode;
    private final String mealDbBaseUrl;

    public RecipeService() {
        this(resolveBaseUrl());
    }

    public RecipeService(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.directMealDbMode = resolveDirectMode();
        this.mealDbBaseUrl = resolveMealDbBaseUrl();
    }

    private static String resolveBaseUrl() {
        String env = System.getenv("RECIPES_API_BASE_URL");
        String prop = System.getProperty("recipes.api.base-url");
        String fallback = "http://localhost:8080/api/v1";
        return env != null && !env.isBlank() ? env : (prop != null && !prop.isBlank() ? prop : fallback);
    }

    private String getInventoryUrl() {
        return baseUrl + "/inventory";
    }

    private String getRecipesUrl() {
        return baseUrl + "/recipes";
    }

    private static boolean resolveDirectMode() {
        // Default to backend (modular). Enable direct mode only if explicitly requested.
        String env = System.getenv("RECIPES_API_DIRECT");
        String prop = System.getProperty("recipes.api.direct");
        if (env != null) return env.equalsIgnoreCase("true") || env.equals("1");
        if (prop != null) return prop.equalsIgnoreCase("true") || prop.equals("1");
        return false;
    }

    private static String resolveMealDbBaseUrl() {
        // Default to test key "1"; allow override if needed
        String env = System.getenv("MEALDB_BASE_URL");
        String prop = System.getProperty("mealdb.base-url");
        String fallback = "https://www.themealdb.com/api/json/v1/1"; // key "1"
        String url = env != null && !env.isBlank() ? env : (prop != null && !prop.isBlank() ? prop : fallback);
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public List<RecipeModels.RecipeCard> search(String query) {
        try {
            if (directMealDbMode) {
                return directSearchMealDb(query);
            }
            String q = URLEncoder.encode(query == null ? "" : query, UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getRecipesUrl() + "/search?q=" + q))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new RuntimeException("Search failed: HTTP " + res.statusCode());
            }
            return mapper.readValue(res.body(), new TypeReference<List<RecipeModels.RecipeCard>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to search recipes", e);
        }
    }

    public RecipeModels.RecipeMeta meta() {
        try {
            if (directMealDbMode) {
                // Direct mode: query MealDB list endpoints
                java.util.List<String> cats = directList(mealDbBaseUrl + "/list.php?c=list", "strCategory");
                java.util.List<String> areas = directList(mealDbBaseUrl + "/list.php?a=list", "strArea");
                return new RecipeModels.RecipeMeta(cats, areas);
            }
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getRecipesUrl() + "/meta"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new RuntimeException("Meta failed: HTTP " + res.statusCode());
            return mapper.readValue(res.body(), RecipeModels.RecipeMeta.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load recipe meta", e);
        }
    }

    public java.util.List<RecipeModels.RecipeCardMatch> filter2(
        java.util.List<String> ingredients,
        boolean matchAll,
        int minMatched,
        String category,
        String area,
        String name,
        int limit,
        int offset
    ) {
        try {
            if (directMealDbMode) {
                // For direct mode, use existing direct filter and synthesize matches as best-effort (matched = size of chips present in title)
                var cards = directFilterMealDb(ingredients, matchAll);
                java.util.ArrayList<RecipeModels.RecipeCardMatch> out = new java.util.ArrayList<>();
                for (var c : cards) {
                    int matched = 0;
                    if (ingredients != null) {
                        for (String chip : ingredients) {
                            if (c.title() != null && c.title().toLowerCase().contains(chip.toLowerCase())) matched++;
                        }
                    }
                    out.add(new RecipeModels.RecipeCardMatch(c.id(), c.title(), c.image(), matched, ingredients == null ? 0 : ingredients.size()));
                }
                return out;
            }
            StringBuilder sb = new StringBuilder(getRecipesUrl()).append("/filter2?match=")
                .append(matchAll ? "all" : "any")
                .append("&minMatched=").append(minMatched)
                .append("&limit=").append(limit)
                .append("&offset=").append(offset);
            if (category != null && !category.isBlank()) sb.append("&category=").append(URLEncoder.encode(category, UTF_8));
            if (area != null && !area.isBlank()) sb.append("&area=").append(URLEncoder.encode(area, UTF_8));
            if (name != null && !name.isBlank()) sb.append("&q=").append(URLEncoder.encode(name, UTF_8));
            if (ingredients != null) {
                for (String ing : ingredients) {
                    if (ing == null || ing.isBlank()) continue;
                    sb.append("&i=").append(URLEncoder.encode(ing, UTF_8));
                }
            }
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(sb.toString()))
                .timeout(Duration.ofSeconds(12))
                .GET()
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new RuntimeException("Filter2 failed: HTTP " + res.statusCode());
            return mapper.readValue(res.body(), new TypeReference<java.util.List<RecipeModels.RecipeCardMatch>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to filter recipes", e);
        }
    }

    public RecipeModels.RecipeDetails details(String id) {
        try {
            if (directMealDbMode) {
                return directDetailsMealDb(id);
            }
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getRecipesUrl() + "/" + URLEncoder.encode(id, UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                if (directMealDbMode) {
                    return directDetailsMealDb(id);
                }
                throw new RuntimeException("Details failed: HTTP " + res.statusCode());
            }
            RecipeModels.RecipeDetails d = mapper.readValue(res.body(), RecipeModels.RecipeDetails.class);
            // If backend returned incomplete ingredient list (common when server model is outdated),
            // fallback to direct MealDB to enrich the data.
            if ((d.ingredients() == null || d.ingredients().size() <= 1) && directMealDbMode) {
                return directDetailsMealDb(id);
            }
            return d;
        } catch (Exception e) {
            if (directMealDbMode) {
                try {
                    // Last-resort fallback
                    return directDetailsMealDb(id);
                } catch (Exception inner) {
                    throw new RuntimeException("Failed to load recipe details", inner);
                }
            }
            throw new RuntimeException("Failed to load recipe details", e);
        }
    }

  public List<RecipeModels.RecipeCard> filterByIngredients(List<String> ingredients, boolean matchAll) {
    try {
      if (directMealDbMode) {
        return directFilterMealDb(ingredients, matchAll);
      }
      if (ingredients == null || ingredients.isEmpty()) return java.util.List.of();
      StringBuilder sb = new StringBuilder(getRecipesUrl()).append("/filter?match=")
          .append(matchAll ? "all" : "any");
      for (String ing : ingredients) {
        if (ing == null || ing.isBlank()) continue;
        sb.append("&i=").append(URLEncoder.encode(ing, UTF_8));
      }
      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(sb.toString()))
          .timeout(Duration.ofSeconds(10))
          .GET()
          .build();
      HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() != 200) {
        if (directMealDbMode) {
          // Fallback to direct MealDB when explicitly enabled
          return directFilterMealDb(ingredients, matchAll);
        }
        throw new RuntimeException("Filter failed: HTTP " + res.statusCode());
      }
      return mapper.readValue(res.body(), new TypeReference<List<RecipeModels.RecipeCard>>() {});
    } catch (Exception e) {
      if (directMealDbMode) {
        try {
          return directFilterMealDb(ingredients, matchAll);
        } catch (Exception inner) {
          throw new RuntimeException("Failed to filter recipes by ingredients", inner);
        }
      }
      throw new RuntimeException("Failed to filter recipes by ingredients", e);
    }
  }

    public void replaceInventory(List<gr2536.core.item.Item> items) {
        try {
            String body = mapper.writeValueAsString(items);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(getInventoryUrl()))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new RuntimeException("Failed to sync fridge with backend: HTTP " + res.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync fridge with backend", e);
        }
    }

  private List<RecipeModels.RecipeCard> directFilterMealDb(List<String> ingredients, boolean matchAll) throws Exception {
    if (ingredients == null || ingredients.isEmpty()) return java.util.List.of();
    java.util.List<java.util.Set<String>> idSets = new java.util.ArrayList<>();
    java.util.Map<String, RecipeModels.RecipeCard> lookup = new java.util.HashMap<>();
    for (String ing : ingredients) {
      String normalized = ing == null ? "" : ing.trim().replace(' ', '_');
      HttpRequest req = HttpRequest.newBuilder()
          .uri(URI.create(mealDbBaseUrl + "/filter.php?i=" + URLEncoder.encode(normalized, UTF_8)))
          .timeout(Duration.ofSeconds(10)).GET().build();
      HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
      if (res.statusCode() != 200) continue;
      JsonNode root = mapper.readTree(res.body());
      JsonNode meals = root.get("meals");
      java.util.Set<String> ids = new java.util.HashSet<>();
      if (meals != null && meals.isArray()) {
        for (JsonNode m : meals) {
          String id = text(m, "idMeal");
          String title = text(m, "strMeal");
          String image = text(m, "strMealThumb");
          if (id != null) {
            ids.add(id);
            lookup.putIfAbsent(id, new RecipeModels.RecipeCard(id, title, image));
          }
        }
      }
      idSets.add(ids);
    }
    if (idSets.isEmpty()) return java.util.List.of();
    java.util.Set<String> resultIds = new java.util.HashSet<>(idSets.get(0));
    if (matchAll) {
      for (int i = 1; i < idSets.size(); i++) resultIds.retainAll(idSets.get(i));
    } else {
      for (int i = 1; i < idSets.size(); i++) resultIds.addAll(idSets.get(i));
    }
    java.util.ArrayList<RecipeModels.RecipeCard> out = new java.util.ArrayList<>();
    for (String id : resultIds) {
      RecipeModels.RecipeCard c = lookup.get(id);
      out.add(c != null ? c : new RecipeModels.RecipeCard(id, id, null));
    }
    return out;
  }
    // ===== Direct TheMealDB integration (fallback/testing) =====
    private List<RecipeModels.RecipeCard> directSearchMealDb(String query) throws Exception {
        String q = URLEncoder.encode(query == null ? "" : query, UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(mealDbBaseUrl + "/search.php?s=" + q))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("MealDB search failed: HTTP " + res.statusCode());
        }
        JsonNode root = mapper.readTree(res.body());
        JsonNode meals = root.get("meals");
        if (meals == null || meals.isNull() || !meals.isArray()) {
            return java.util.List.of();
        }
        java.util.ArrayList<RecipeModels.RecipeCard> out = new java.util.ArrayList<>();
        for (JsonNode m : meals) {
            String id = text(m, "idMeal");
            String title = text(m, "strMeal");
            String image = text(m, "strMealThumb");
            if (id != null && title != null) {
                out.add(new RecipeModels.RecipeCard(id, title, image));
            }
        }
        return out;
    }

    private RecipeModels.RecipeDetails directDetailsMealDb(String id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(mealDbBaseUrl + "/lookup.php?i=" + URLEncoder.encode(id, UTF_8)))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("MealDB details failed: HTTP " + res.statusCode());
        }
        JsonNode root = mapper.readTree(res.body());
        JsonNode meals = root.get("meals");
        if (meals == null || meals.isNull() || !meals.isArray() || meals.size() == 0) {
            throw new RuntimeException("Recipe not found: " + id);
        }
        JsonNode m = meals.get(0);
        String title = text(m, "strMeal");
        String image = text(m, "strMealThumb");
        String category = text(m, "strCategory");
        String area = text(m, "strArea");
        String instructions = text(m, "strInstructions");
        String youtube = text(m, "strYoutube");

        java.util.ArrayList<RecipeModels.IngredientDto> ingredients = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String name = text(m, "strIngredient" + i);
            String measure = text(m, "strMeasure" + i);
            if (name != null && !name.isBlank()) {
                ingredients.add(new RecipeModels.IngredientDto(name.trim(), measure == null ? "" : measure.trim()));
            }
        }
        return new RecipeModels.RecipeDetails(id, title, image, category, area, ingredients, instructions, youtube);
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private java.util.List<String> directList(String url, String field) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("MealDB list failed: HTTP " + res.statusCode());
        }
        JsonNode root = mapper.readTree(res.body());
        JsonNode meals = root.get("meals");
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        if (meals != null && meals.isArray()) {
            for (JsonNode m : meals) {
                String val = text(m, field);
                if (val != null && !val.isBlank()) out.add(val);
            }
        }
        out.sort(String::compareToIgnoreCase);
        return out;
    }
}


