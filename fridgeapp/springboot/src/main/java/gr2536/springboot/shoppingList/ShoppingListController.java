package gr2536.springboot.shoppingList;

import gr2536.core.item.Item;
import gr2536.springboot.fridge.dto.CreateItemRequest;
import gr2536.springboot.fridge.dto.RemoveResult;
import gr2536.springboot.shoppingList.dto.AddToFridgeRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shopping-list")
@Validated
@RequiredArgsConstructor
public class ShoppingListController {

  private final ShoppingListService svc;

  @GetMapping("/items")
  public List<Item> list() {
    return svc.list();
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public Item add(@Valid @RequestBody CreateItemRequest req) {
    return svc.add(req);
  }

  @DeleteMapping("/items/{name}")
  public ResponseEntity<RemoveResult> remove(@PathVariable String name, @RequestParam("qty") @Positive int qty) {
    int remaining = svc.remove(name, qty);
    return ResponseEntity.ok(new RemoveResult(name, remaining));
  }

  @PostMapping("/actions/add-to-fridge")
  public ResponseEntity<?> addToFridge(@RequestBody(required = false) AddToFridgeRequest body) {
    boolean clear = body == null || body.clearListAfter();
    svc.moveAllToFridge(clear);
    return ResponseEntity.ok().build();
  }
}
