package gr2536.springboot.recipes;

import gr2536.core.fridge.Fridge;
import gr2536.core.item.Item;
import gr2536.core.recipes.RecipeMatcher;
import gr2536.springboot.recipes.dto.RecipeCard;
import gr2536.springboot.recipes.dto.RecipeCardMatch;
import gr2536.springboot.recipes.dto.RecipeDetails;
import gr2536.springboot.recipes.dto.RecipeMeta;
import gr2536.springboot.recipes.mealDb.MealDbClient;
import gr2536.springboot.recipes.mealDb.MealDbListResponse;
import gr2536.springboot.recipes.mealDb.MealDbMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {
  private static final Logger log = Logger.getLogger(RecipeService.class.getName());
  private static final MealDbThrottle MEAL_DB_THROTTLE = new MealDbThrottle(Duration.ofMillis(50));
  private static final int VERIFICATION_LIMIT = 30;

  private final MealDbClient client;
  private final Fridge fridge;
  private final RecipeMatcher matcher = new RecipeMatcher();

  public List<RecipeCard> search(String q) {
    MEAL_DB_THROTTLE.acquire();
    var resp = client.searchByName(q);
    return MealDbMapper.toCards(resp);
  }

  public RecipeDetails byId(String id) {
    MEAL_DB_THROTTLE.acquire();
    var resp = client.lookupById(id);
    if (resp == null) {
      return null;
    }
    var meals = resp.meals();
    if (meals.isEmpty()) {
      return null;
    }
    return MealDbMapper.toDetail(meals.get(0));
  }

  public List<RecipeCard> filterByIngredients(List<String> ingredients, boolean matchAll) {
    if (ingredients == null || ingredients.isEmpty()) {
      return List.of();
    }

    Map<String, List<RecipeCard>> perIngredient = new LinkedHashMap<>();
    for (String raw : ingredients) {
      if (raw == null || raw.isBlank()) {
        continue;
      }
      String ingredient = raw.trim();
      try {
        MEAL_DB_THROTTLE.acquire();
        var resp = client.filterByIngredient(ingredient);
        var cards = MealDbMapper.toCards(resp);
        if (!cards.isEmpty()) {
          perIngredient.put(ingredient, cards);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to filter recipes by ingredient '%s': %s", ingredient, e.getMessage()));
        log.log(Level.FINE, String.format("Filter-by-ingredient failure for '%s'", ingredient), e);
      }
    }

    if (perIngredient.isEmpty()) {
      return List.of();
    }

    List<Set<String>> idSets = new ArrayList<>();
    for (List<RecipeCard> cards : perIngredient.values()) {
      Set<String> ids = cards.stream()
          .map(RecipeCard::id)
          .collect(Collectors.toCollection(LinkedHashSet::new));
      if (!ids.isEmpty()) {
        idSets.add(ids);
      }
    }

    if (idSets.isEmpty()) {
      return List.of();
    }

    Set<String> resultIds;
    if (matchAll) {
      var iterator = idSets.iterator();
      LinkedHashSet<String> intersection = new LinkedHashSet<>(iterator.next());
      while (iterator.hasNext()) {
        intersection.retainAll(iterator.next());
      }
      resultIds = intersection;
    } else {
      LinkedHashSet<String> union = new LinkedHashSet<>();
      for (List<RecipeCard> cards : perIngredient.values()) {
        for (RecipeCard card : cards) {
          union.add(card.id());
        }
      }
      resultIds = union;
    }

    if (resultIds.isEmpty()) {
      return List.of();
    }

    Map<String, RecipeCard> lookup = perIngredient.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toMap(RecipeCard::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));

    return resultIds.stream()
        .map(id -> lookup.getOrDefault(id, new RecipeCard(id, id, null)))
        .toList();
  }

  /**
   * Filter by ingredients with verification. Matching is computed against the current fridge
   * contents using the core RecipeMatcher, and minMatched applies to satisfied ingredients.
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
    List<Item> fridgeItemsSnapshot = fridge.listItems();

    if (log.isLoggable(Level.FINE)) {
      log.fine(String.format("FILTER: Received request with %d ingredient chips.", chips.size()));
    }

    // Track if ingredients were originally provided (before any auto-derivation)
    boolean hadOriginalIngredients = !chips.isEmpty();
    
    // If no chips were provided, derive candidates from the fridge contents.
    // BUT: Skip auto-derivation if filtering by category/area/name only (meta filters)
    // This allows fast category filtering without ingredient matching
    boolean hasMetaFilters = (category != null && !category.isBlank())
                          || (area != null && !area.isBlank())
                          || (nameQuery != null && !nameQuery.isBlank());
    
    boolean derivedFromFridge = false;
    boolean hasFridgeItems = !fridgeItemsSnapshot.isEmpty();
    if (log.isLoggable(Level.FINE)) {
      log.fine(String.format("FILTER: Server fridge has items? %s", hasFridgeItems));
    }

    // Only auto-derive from fridge if no meta filters are specified
    // (when filtering by category/area/name, we want fast filtering without ingredient matching)
    if (chips.isEmpty() && hasFridgeItems && !hasMetaFilters) {
      List<String> fridgeChips = fridgeItemsSnapshot.stream()
          .map(Item::getName)
          .filter(n -> n != null && !n.isBlank())
          .distinct()
          .limit(10) // Reduced from 25 to avoid API rate limits
          .toList();
      if (!fridgeChips.isEmpty()) {
        chips = fridgeChips;
        derivedFromFridge = true;
        if (log.isLoggable(Level.FINE)) {
          log.fine(String.format("FILTER: Using %d ingredients from synced fridge: %s", chips.size(), String.join(", ", chips)));
        }
      }
    }

    Set<String> candidateIds;
    Map<String, RecipeCard> lightLookup = Map.of();
    if (chips.isEmpty()) {
      SeedCandidates seeded = seedCandidatesFromMeta(category, area, nameQuery);
      candidateIds = seeded.ids();
      lightLookup = seeded.cards();
    } else {
      boolean useAll = derivedFromFridge ? false : matchAll; // union when derived from fridge
      SeedCandidates seeded = seedCandidatesFromChips(chips, useAll);
      candidateIds = seeded.ids();
      lightLookup = seeded.cards();

      // If meta filters (category/area/name) are specified, intersect with those candidates
      if (hasMetaFilters && !candidateIds.isEmpty()) {
        SeedCandidates metaSeeded = seedCandidatesFromMeta(category, area, nameQuery);
        if (!metaSeeded.ids().isEmpty()) {
          candidateIds = candidateIds.stream()
              .filter(metaSeeded.ids()::contains)
              .collect(Collectors.toCollection(LinkedHashSet::new));
          // Merge lookup maps, preferring existing entries
          Map<String, RecipeCard> mergedLookup = new LinkedHashMap<>(lightLookup);
          metaSeeded.cards().forEach((id, card) -> mergedLookup.putIfAbsent(id, card));
          lightLookup = mergedLookup;
        }
      }
    }
    if (candidateIds.isEmpty()) {
      return List.of();
    }

    int safeOffset = Math.max(0, offset);
    int requestedLimit = limit > 0 ? limit : Integer.MAX_VALUE;

    // When fridge is empty OR we're filtering by meta only, don't enforce minMatched
    int effectiveMinMatched = (!hasFridgeItems || chips.isEmpty()) ? 0 : Math.max(0, minMatched);
    var params = new FilterParams(chips, effectiveMinMatched, category, area, nameQuery);

    // Optimization: Skip expensive verification when filtering by category/area/name only
    // If no ingredients were originally provided AND we have meta filters, skip verification
    // This allows fast category filtering without ingredient matching
    boolean skipVerification = !hadOriginalIngredients && hasMetaFilters;
    
    if (log.isLoggable(Level.FINE)) {
      log.fine(String.format("FILTER: skipVerification=%s (hadOriginalIngredients=%s, hasMetaFilters=%s)", 
          skipVerification, hadOriginalIngredients, hasMetaFilters));
    }

    List<String> orderedCandidates = new ArrayList<>(candidateIds);
    List<RecipeCardMatch> verified;
    if (skipVerification) {
      // Fast path: Fetch recipe details only until we've satisfied the requested page.
      verified = new ArrayList<>();
      int satisfiedBeforeOffset = 0;
      for (String id : orderedCandidates) {
        if (requestedLimit != Integer.MAX_VALUE && verified.size() >= requestedLimit) {
          break;
        }
        RecipeCard card = lightLookup.get(id);
        int ingredientCount = 0;
        
        // Fetch recipe detail to get ingredient count
        try {
          MEAL_DB_THROTTLE.acquire();
          var detail = client.lookupById(id);
          if (detail == null) {
            continue;
          }
          var meals = detail.meals();
          if (meals.isEmpty()) {
            continue;
          }
          var dto = MealDbMapper.toDetail(meals.get(0));
          if (!passesConstraints(dto, params.category(), params.area(), params.nameQuery())) {
            continue;
          }
          
          // Get ingredient count from the recipe
          ingredientCount = dto.ingredients() != null ? dto.ingredients().size() : 0;
          
          // Update card if we don't have it or need to refresh
          if (card == null) {
            card = new RecipeCard(id, dto.title(), dto.image());
          }
        } catch (Exception e) {
          log.log(Level.FINE, String.format("Failed to fetch recipe '%s' in fast path: %s", id, e.getMessage()));
          // If fetch fails but we have a card, use it with 0 count
          if (card == null) {
            continue;
          }
        }
        
        if (satisfiedBeforeOffset < safeOffset) {
          satisfiedBeforeOffset++;
          continue;
        }

        // No ingredient matching needed, so matched=0, but we have the total count
        verified.add(new RecipeCardMatch(card.id(), card.title(), card.image(), 0, ingredientCount));
      }
      return verified;
    } else {
      // Full verification path: Limit candidates and verify each one
      long desiredCount = requestedLimit == Integer.MAX_VALUE
          ? Long.MAX_VALUE
          : (long) safeOffset + requestedLimit;
      int maxToCheck;
      if (desiredCount == Long.MAX_VALUE) {
        maxToCheck = orderedCandidates.size();
      } else {
        maxToCheck = (int) Math.min(orderedCandidates.size(), Math.max(VERIFICATION_LIMIT, desiredCount));
      }

      Set<String> limitedCandidates = orderedCandidates.stream()
          .limit(maxToCheck)
          .collect(Collectors.toCollection(LinkedHashSet::new));

      verified = new ArrayList<>();
      for (String id : limitedCandidates) {
        try {
          var match = verifyAndScore(id, lightLookup, params, fridgeItemsSnapshot);
          if (match != null) {
            verified.add(match);
          }
        } catch (Exception e) {
          log.log(Level.WARNING, String.format("Failed to verify recipe '%s': %s", id, e.getMessage()));
          log.log(Level.FINE, String.format("Verification failure for recipe %s", id), e);
        }
      }
      verified.sort((a, b) -> Integer.compare(b.matched(), a.matched()));
    }

    long limitToApply = (requestedLimit == Integer.MAX_VALUE) ? Long.MAX_VALUE : requestedLimit;
    return verified.stream()
        .skip(safeOffset)
        .limit(limitToApply)
        .toList();
  }

  // -------- Helpers --------
  private static String normalize(String s) {
    if (s == null) {
      return "";
    }
    // Preserve original casing and characters; collapse whitespace only.
    String x = s.trim();
    x = x.replaceAll("\\s+", " ");
    return x;
  }

  private static List<String> normalizeChips(List<String> ingredients) {
    return (ingredients == null ? java.util.List.<String>of() : ingredients).stream()
        .filter(s -> s != null && !s.isBlank())
        .map(RecipeService::normalize)
        .distinct()
        .toList();
  }

  private SeedCandidates seedCandidatesFromChips(List<String> chips, boolean matchAll) {
    Map<String, List<RecipeCard>> perIngredient = new LinkedHashMap<>();
    for (String chip : chips) {
      try {
        MEAL_DB_THROTTLE.acquire();
        var resp = client.filterByIngredient(chip);
        var cards = MealDbMapper.toCards(resp);
        if (!cards.isEmpty()) {
          perIngredient.put(chip, cards);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to filter by ingredient '%s': %s", chip, e.getMessage()));
        log.log(Level.FINE, String.format("Filter-by-ingredient failure for '%s'", chip), e);
      }
    }
    if (perIngredient.isEmpty()) {
      return new SeedCandidates(Set.of(), Map.of());
    }

    List<Set<String>> idSets = new ArrayList<>();
    for (List<RecipeCard> cards : perIngredient.values()) {
      Set<String> ids = cards.stream()
          .map(RecipeCard::id)
          .collect(Collectors.toCollection(LinkedHashSet::new));
      if (!ids.isEmpty()) {
        idSets.add(ids);
      }
    }
    if (idSets.isEmpty()) {
      return new SeedCandidates(Set.of(), Map.of());
    }

    Set<String> ids;
    if (matchAll) {
      var iterator = idSets.iterator();
      LinkedHashSet<String> intersection = new LinkedHashSet<>(iterator.next());
      while (iterator.hasNext()) {
        intersection.retainAll(iterator.next());
      }
      ids = intersection;
    } else {
      LinkedHashSet<String> union = new LinkedHashSet<>();
      for (List<RecipeCard> cards : perIngredient.values()) {
        for (RecipeCard card : cards) {
          union.add(card.id());
        }
      }
      ids = union;
    }

    Map<String, RecipeCard> lookup = perIngredient.values().stream()
        .flatMap(List::stream)
        .collect(Collectors.toMap(RecipeCard::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    return new SeedCandidates(ids, lookup);
  }

  private SeedCandidates seedCandidatesFromMeta(String category, String area, String nameQuery) {
    LinkedHashSet<String> byName = new LinkedHashSet<>();
    Map<String, RecipeCard> nameLookup = new LinkedHashMap<>();
    if (nameQuery != null && !nameQuery.isBlank()) {
      try {
        MEAL_DB_THROTTLE.acquire();
        var resp = client.searchByName(nameQuery);
        List<RecipeCard> cards = MealDbMapper.toCards(resp);
        for (RecipeCard card : cards) {
          byName.add(card.id());
          nameLookup.put(card.id(), card);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to seed recipes by name query '%s': %s", nameQuery, e.getMessage()));
        log.log(Level.FINE, String.format("Name query seeding failure for '%s'", nameQuery), e);
      }
    }

    LinkedHashSet<String> byCat = new LinkedHashSet<>();
    Map<String, RecipeCard> catLookup = new LinkedHashMap<>();
    if (category != null && !category.isBlank()) {
      try {
        MEAL_DB_THROTTLE.acquire();
        var resp = client.filterByCategory(category);
        List<RecipeCard> cards = MealDbMapper.toCards(resp);
        for (RecipeCard card : cards) {
          byCat.add(card.id());
          catLookup.put(card.id(), card);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to seed recipes by category '%s': %s", category, e.getMessage()));
        log.log(Level.FINE, String.format("Category seeding failure for '%s'", category), e);
      }
    }

    LinkedHashSet<String> byArea = new LinkedHashSet<>();
    Map<String, RecipeCard> areaLookup = new LinkedHashMap<>();
    if (area != null && !area.isBlank()) {
      try {
        MEAL_DB_THROTTLE.acquire();
        var resp = client.filterByArea(area);
        List<RecipeCard> cards = MealDbMapper.toCards(resp);
        for (RecipeCard card : cards) {
          byArea.add(card.id());
          areaLookup.put(card.id(), card);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to seed recipes by area '%s': %s", area, e.getMessage()));
        log.log(Level.FINE, String.format("Area seeding failure for '%s'", area), e);
      }
    }

    List<Set<String>> nonEmpty = new ArrayList<>();
    if (!byName.isEmpty()) {
      nonEmpty.add(byName);
    }
    if (!byCat.isEmpty()) {
      nonEmpty.add(byCat);
    }
    if (!byArea.isEmpty()) {
      nonEmpty.add(byArea);
    }
    if (nonEmpty.isEmpty()) {
      return new SeedCandidates(Set.of(), Map.of());
    }

    LinkedHashSet<String> ids = new LinkedHashSet<>(nonEmpty.get(0));
    for (int i = 1; i < nonEmpty.size(); i++) {
      ids.retainAll(nonEmpty.get(i));
    }
    
    // Build lookup map from intersection - prefer cards from first non-empty set
    Map<String, RecipeCard> lookup = new LinkedHashMap<>();
    if (!byName.isEmpty()) {
      for (String id : ids) {
        if (nameLookup.containsKey(id)) {
          lookup.put(id, nameLookup.get(id));
        }
      }
    }
    if (!byCat.isEmpty()) {
      for (String id : ids) {
        lookup.putIfAbsent(id, catLookup.get(id));
      }
    }
    if (!byArea.isEmpty()) {
      for (String id : ids) {
        lookup.putIfAbsent(id, areaLookup.get(id));
      }
    }
    
    return new SeedCandidates(ids, lookup);
  }

  private RecipeCardMatch verifyAndScore(
      String id,
      Map<String, RecipeCard> light,
      FilterParams params,
      List<Item> fridgeItems
  ) {
    MEAL_DB_THROTTLE.acquire();
    var detail = client.lookupById(id);
    if (detail == null) {
      return null;
    }
    var meals = detail.meals();
    if (meals.isEmpty()) {
      return null;
    }
    var dto = MealDbMapper.toDetail(meals.get(0));
    if (!passesConstraints(dto, params.category(), params.area(), params.nameQuery())) {
      return null;
    }

    // Build core recipe, compute match vs current fridge items
    var coreRecipe = RecipeDomainMapper.toCoreRecipe(dto);
    
    // If fridge is empty, treat all recipes as having 0 matched ingredients
    int matched = 0;
    int total = coreRecipe.getIngredients().size();
    
    if (!fridgeItems.isEmpty()) {
      var matches = matcher.findMatches(List.of(coreRecipe), fridgeItems);
      if (matches.isEmpty()) {
        return null;
      }
      var match = matches.get(0);
      matched = match.getMatchedIngredients();
      total = match.getTotalIngredients();
    }
    
    if (matched < params.minMatched()) {
      return null;
    }

    var base = light.getOrDefault(id, new RecipeCard(id, dto.title(), dto.image()));
    return new RecipeCardMatch(base.id(), base.title(), base.image(), matched, total);
  }

  private boolean passesConstraints(RecipeDetails dto, String category, String area, String nameQuery) {
    if (category != null && !category.isBlank()) {
      if (dto.category() == null || !dto.category().equalsIgnoreCase(category)) {
        return false;
      }
    }
    if (area != null && !area.isBlank()) {
      if (dto.area() == null || !dto.area().equalsIgnoreCase(area)) {
        return false;
      }
    }
    if (nameQuery != null && !nameQuery.isBlank()) {
      if (dto.title() == null || !dto.title().toLowerCase(Locale.ROOT).contains(nameQuery.toLowerCase(Locale.ROOT))) {
        return false;
      }
    }
    return true;
  }

  private record FilterParams(List<String> chips, int minMatched, String category, String area, String nameQuery) {}
  private record SeedCandidates(Set<String> ids, Map<String, RecipeCard> cards) {}

  public RecipeMeta meta() {
    List<String> categories = List.of();
    try {
      MEAL_DB_THROTTLE.acquire();
      categories = List.copyOf(extract(client.listCategories(), true));
    } catch (Exception e) {
      log.log(Level.WARNING, String.format("Failed to fetch recipe categories: %s", e.getMessage()));
      log.log(Level.FINE, "Category fetch failure", e);
    }

    List<String> areas = List.of();
    try {
      MEAL_DB_THROTTLE.acquire();
      areas = List.copyOf(extract(client.listAreas(), false));
    } catch (Exception e) {
      log.log(Level.WARNING, String.format("Failed to fetch recipe areas: %s", e.getMessage()));
      log.log(Level.FINE, "Area fetch failure", e);
    }

    return new RecipeMeta(categories, areas);
  }

  private static List<String> extract(MealDbListResponse r, boolean isCategory) {
    if (r == null || r.meals() == null) {
      return List.of();
    }
    return r.meals().stream()
        .map(v -> isCategory ? v.strCategory() : v.strArea())
        .filter(s -> s != null && !s.isBlank())
        .distinct()
        .sorted(String::compareToIgnoreCase)
        .toList();
  }

  private static final class MealDbThrottle {
    private final long minIntervalNanos;
    private long nextAllowedTime;

    private MealDbThrottle(Duration interval) {
      this.minIntervalNanos = Math.max(0, interval.toNanos());
      this.nextAllowedTime = 0L;
    }

    void acquire() {
      if (minIntervalNanos == 0) {
        return;
      }
      long wait = calculateWaitNanos();
      if (wait <= 0) {
        return;
      }
      LockSupport.parkNanos(wait);
      if (Thread.interrupted()) {
        Thread.currentThread().interrupt();
      }
    }

    private synchronized long calculateWaitNanos() {
      long now = System.nanoTime();
      if (now >= nextAllowedTime) {
        nextAllowedTime = now + minIntervalNanos;
        return 0L;
      }
      long wait = nextAllowedTime - now;
      nextAllowedTime += minIntervalNanos;
      return wait;
    }
  }
}
