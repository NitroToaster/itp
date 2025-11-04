package gr2536.springboot.recipes;

import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeDetails;
import gr2536.springboot.recipes.dto.RecipeCardMatch;
import gr2536.springboot.recipes.dto.RecipeMeta;
import gr2536.springboot.recipes.mealDb.MealDbClient;
import gr2536.springboot.recipes.mealDb.MealDbMapper;
import gr2536.springboot.recipes.mealDb.MealDbListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

  public List<RecipeCard> filterByIngredients(List<String> ingredients, boolean matchAll) {
    if (ingredients == null || ingredients.isEmpty()) {
      return List.of();
    }

    // Fetch results per ingredient, then intersect or union their IDs
    List<Set<String>> idSets = ingredients.stream()
        .filter(s -> s != null && !s.isBlank())
        .map(s -> client.filterByIngredient(s))
        .map(MealDbMapper::toCards)
        .map(list -> list.stream().map(RecipeCard::id).collect(Collectors.toSet()))
        .toList();

    if (idSets.isEmpty()) return List.of();

    Set<String> resultIds;
    if (matchAll) {
      resultIds = idSets.get(0);
      for (int i = 1; i < idSets.size(); i++) {
        resultIds.retainAll(idSets.get(i));
      }
    } else {
      resultIds = idSets.stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    if (resultIds.isEmpty()) return List.of();

    // Convert IDs back to cards by merging all fetched lists into a lookup map
    var lookup = ingredients.stream()
        .map(client::filterByIngredient)
        .map(MealDbMapper::toCards)
        .flatMap(List::stream)
        .collect(Collectors.toMap(RecipeCard::id, c -> c, (a, b) -> a));

    return resultIds.stream()
        .map(id -> lookup.getOrDefault(id, new RecipeCard(id, id, null)))
        .toList();
  }

  /**
   * Filter by ingredients with verification using full recipe details to compute matched count.
   * Supports union/intersection and additional client-side constraints.
   */
  public List<RecipeCardMatch> filterWithVerify(
      List<String> ingredients,
      boolean matchAll,
      int minMatched,
      String category,
      String area,
      String nameQuery,
      int limit,
      int offset
  ) {
    List<String> chips = normalizeChips(ingredients);
    Set<String> candidateIds = chips.isEmpty()
        ? seedCandidatesFromMeta(category, area, nameQuery)
        : seedCandidatesFromChips(chips, matchAll);
    if (candidateIds.isEmpty()) return List.of();

    Map<String, RecipeCard> light = buildLightLookup(chips);
    var params = new FilterParams(chips, Math.max(1, minMatched), category, area, nameQuery);
    return candidateIds.stream()
        .skip(Math.max(0, offset))
        .limit(Math.max(1, limit))
        .map(id -> verifyAndScore(id, light, params))
        .filter(java.util.Objects::nonNull)
        .sorted((a, b) -> Integer.compare(b.matched(), a.matched()))
        .toList();
  }

  // -------- Helpers --------
  private static String normalize(String s) {
    if (s == null) return "";
    String x = s.trim().toLowerCase(Locale.ROOT);
    x = x.replace('-', ' ').replace('_', ' ');
    x = x.replaceAll("[^a-z0-9 ]", "");
    x = x.replaceAll("\\s+", " ");
    return x;
  }

  private static List<String> synonyms(String normalized) {
    // Minimal synonym support without external deps
    return switch (normalized) {
      case "bell pepper", "capsicum" -> java.util.List.of("bell pepper", "capsicum");
      default -> java.util.List.of();
    };
  }

  private static List<String> normalizeChips(List<String> ingredients) {
    return (ingredients == null ? java.util.List.<String>of() : ingredients).stream()
        .filter(s -> s != null && !s.isBlank())
        .map(RecipeService::normalize)
        .distinct()
        .toList();
  }

  private Set<String> seedCandidatesFromChips(List<String> chips, boolean matchAll) {
    List<Set<String>> perIng = chips.stream()
        .map(client::filterByIngredient)
        .map(MealDbMapper::toCards)
        .map(list -> list.stream().map(RecipeCard::id).collect(Collectors.toSet()))
        .toList();
    if (perIng.isEmpty()) return java.util.Set.of();
    if (matchAll) {
      Set<String> ids = new java.util.HashSet<>(perIng.get(0));
      for (int i = 1; i < perIng.size(); i++) ids.retainAll(perIng.get(i));
      return ids;
    }
    return perIng.stream().flatMap(Set::stream).collect(Collectors.toSet());
  }

  private Set<String> seedCandidatesFromMeta(String category, String area, String nameQuery) {
    java.util.Set<String> byName = new java.util.HashSet<>();
    if (nameQuery != null && !nameQuery.isBlank()) {
      var resp = client.searchByName(nameQuery);
      byName.addAll(MealDbMapper.toCards(resp).stream().map(RecipeCard::id).toList());
    }
    java.util.Set<String> byCat = new java.util.HashSet<>();
    if (category != null && !category.isBlank()) {
      var resp = client.filterByCategory(category);
      byCat.addAll(MealDbMapper.toCards(resp).stream().map(RecipeCard::id).toList());
    }
    java.util.Set<String> byArea = new java.util.HashSet<>();
    if (area != null && !area.isBlank()) {
      var resp = client.filterByArea(area);
      byArea.addAll(MealDbMapper.toCards(resp).stream().map(RecipeCard::id).toList());
    }
    java.util.List<java.util.Set<String>> nonEmpty = new java.util.ArrayList<>();
    if (!byName.isEmpty()) nonEmpty.add(byName);
    if (!byCat.isEmpty()) nonEmpty.add(byCat);
    if (!byArea.isEmpty()) nonEmpty.add(byArea);
    if (nonEmpty.isEmpty()) return java.util.Set.of();
    java.util.Set<String> ids = new java.util.HashSet<>(nonEmpty.get(0));
    for (int i = 1; i < nonEmpty.size(); i++) ids.retainAll(nonEmpty.get(i));
    return ids;
  }

  private Map<String, RecipeCard> buildLightLookup(List<String> chips) {
    if (chips.isEmpty()) return java.util.Map.of();
    return chips.stream()
        .map(client::filterByIngredient)
        .map(MealDbMapper::toCards)
        .flatMap(List::stream)
        .collect(Collectors.toMap(RecipeCard::id, c -> c, (a, b) -> a));
  }

  private RecipeCardMatch verifyAndScore(String id, Map<String, RecipeCard> light, FilterParams params) {
    var detail = client.lookupById(id);
    if (detail == null || detail.meals() == null || detail.meals().isEmpty()) return null;
    var dto = MealDbMapper.toDetail(detail.meals().get(0));
    int total = dto.ingredients() == null ? 0 : dto.ingredients().size();
    int matched = 0;
    if (!params.chips().isEmpty()) {
      matched = (int) dto.ingredients().stream()
          .map(i -> normalize(i.name()))
          .filter(n -> !n.isBlank())
          .filter(n -> params.chips().contains(n) || synonyms(n).stream().anyMatch(params.chips()::contains))
          .distinct()
          .count();
      if (matched < params.minMatched()) return null;
    }
    if (!passesConstraints(dto, params.category(), params.area(), params.nameQuery())) return null;
    var base = light.getOrDefault(id, new RecipeCard(id, dto.title(), dto.image()));
    return new RecipeCardMatch(base.id(), base.title(), base.image(), matched, total);
  }

  private boolean passesConstraints(RecipeDetails dto, String category, String area, String nameQuery) {
    if (category != null && !category.isBlank()) {
      if (dto.category() == null || !dto.category().equalsIgnoreCase(category)) return false;
    }
    if (area != null && !area.isBlank()) {
      if (dto.area() == null || !dto.area().equalsIgnoreCase(area)) return false;
    }
    if (nameQuery != null && !nameQuery.isBlank()) {
      if (dto.title() == null || !dto.title().toLowerCase(Locale.ROOT).contains(nameQuery.toLowerCase(Locale.ROOT))) return false;
    }
    return true;
  }

  private record FilterParams(List<String> chips, int minMatched, String category, String area, String nameQuery) {}

  public RecipeMeta meta() {
    List<String> categories = List.copyOf(extract(client.listCategories(), true));
    List<String> areas = List.copyOf(extract(client.listAreas(), false));
    return new RecipeMeta(categories, areas);
  }

  private static List<String> extract(MealDbListResponse r, boolean isCategory) {
    if (r == null || r.meals() == null) return List.of();
    return r.meals().stream()
        .map(v -> isCategory ? v.strCategory() : v.strArea())
        .filter(s -> s != null && !s.isBlank())
        .distinct()
        .sorted(String::compareToIgnoreCase)
        .toList();
  }
}
