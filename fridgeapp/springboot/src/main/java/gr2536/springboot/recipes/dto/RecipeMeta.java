package gr2536.springboot.recipes.dto;

import java.util.List;

/** Categories and Areas to drive filters in the UI. */
public record RecipeMeta(List<String> categories, List<String> areas) {
  public RecipeMeta {
    categories = categories == null ? List.of() : List.copyOf(categories);
    areas = areas == null ? List.of() : List.copyOf(areas);
  }
}

