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
        String fallback = "http://localhost:8080/api/v1/recipes";
        return env != null && !env.isBlank() ? env : (prop != null && !prop.isBlank() ? prop : fallback);
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
                .uri(URI.create(baseUrl + "/search?q=" + q))
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

    public RecipeModels.RecipeDetails details(String id) {
        try {
            if (directMealDbMode) {
                return directDetailsMealDb(id);
            }
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + URLEncoder.encode(id, UTF_8)))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new RuntimeException("Details failed: HTTP " + res.statusCode());
            }
            return mapper.readValue(res.body(), RecipeModels.RecipeDetails.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load recipe details", e);
        }
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
}


