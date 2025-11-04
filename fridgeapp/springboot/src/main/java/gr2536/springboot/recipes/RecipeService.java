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
    // Normalize inputs once
    List<String> chips = (ingredients == null ? java.util.List.<String>of() : ingredients).stream()
        .filter(s -> s != null && !s.isBlank())
        .map(RecipeService::normalize)
        .distinct()
        .toList();
    Set<String> candidateIds;
    if (chips.isEmpty()) {
      // Category/Area/Name-only filtering path
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
      // Combine: if multiple provided, intersect; otherwise union what exists
      java.util.List<java.util.Set<String>> nonEmpty = new java.util.ArrayList<>();
      if (!byName.isEmpty()) nonEmpty.add(byName);
      if (!byCat.isEmpty()) nonEmpty.add(byCat);
      if (!byArea.isEmpty()) nonEmpty.add(byArea);
      if (nonEmpty.isEmpty()) return List.of();
      candidateIds = new java.util.HashSet<>(nonEmpty.get(0));
      for (int i = 1; i < nonEmpty.size(); i++) candidateIds.retainAll(nonEmpty.get(i));
    } else {
      // Seed candidates from MealDB filter per ingredient
      List<Set<String>> perIng = chips.stream()
          .map(orig -> client.filterByIngredient(orig))
          .map(MealDbMapper::toCards)
          .map(list -> list.stream().map(RecipeCard::id).collect(Collectors.toSet()))
          .toList();
      if (perIng.isEmpty()) return List.of();
      if (matchAll) {
        candidateIds = perIng.get(0);
        for (int i = 1; i < perIng.size(); i++) candidateIds.retainAll(perIng.get(i));
      } else {
        candidateIds = perIng.stream().flatMap(Set::stream).collect(Collectors.toSet());
      }
      if (candidateIds.isEmpty()) return List.of();
    }

    // Build light lookup for title/thumb from first ingredient fetch to avoid extra calls
    Map<String, RecipeCard> light = chips.stream()
        .map(client::filterByIngredient)
        .map(MealDbMapper::toCards)
        .flatMap(List::stream)
        .collect(Collectors.toMap(RecipeCard::id, c -> c, (a, b) -> a));

    // Verify and score by fetching details and checking ingredients properly
    var scored = candidateIds.stream()
        .skip(Math.max(0, offset))
        .limit(Math.max(1, limit))
        .map(id -> {
          var detail = client.lookupById(id);
          if (detail == null || detail.meals() == null || detail.meals().isEmpty()) return null;
          var dto = MealDbMapper.toDetail(detail.meals().get(0));
          int total = dto.ingredients() == null ? 0 : dto.ingredients().size();
          int matched = 0;
          if (!chips.isEmpty()) {
            matched = (int) dto.ingredients().stream()
                .map(i -> normalize(i.name()))
                .filter(n -> !n.isBlank())
                .filter(n -> chips.contains(n) || synonyms(n).stream().anyMatch(chips::contains))
                .distinct()
                .count();
            if (matched < Math.max(1, minMatched)) return null;
          }
          if (category != null && !category.isBlank()) {
            if (dto.category() == null || !dto.category().equalsIgnoreCase(category)) return null;
          }
          if (area != null && !area.isBlank()) {
            if (dto.area() == null || !dto.area().equalsIgnoreCase(area)) return null;
          }
          if (nameQuery != null && !nameQuery.isBlank()) {
            if (dto.title() == null || !dto.title().toLowerCase().contains(nameQuery.toLowerCase())) return null;
          }
          var base = light.getOrDefault(id, new RecipeCard(id, dto.title(), dto.image()));
          return new RecipeCardMatch(base.id(), base.title(), base.image(), matched, total);
        })
        .filter(java.util.Objects::nonNull)
        .sorted((a, b) -> Integer.compare(b.matched(), a.matched()))
        .toList();

    return scored;
  }

  // -------- Helpers --------
  private static String normalize(String s) {
    if (s == null) return "";
    String x = s.trim().toLowerCase();
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

  public RecipeMeta meta() {
    List<String> categories = extract(client.listCategories(), true);
    List<String> areas = extract(client.listAreas(), false);
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
