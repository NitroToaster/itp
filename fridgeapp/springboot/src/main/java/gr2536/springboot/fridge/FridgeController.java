package gr2536.springboot.fridge;

import gr2536.core.item.Item;
import gr2536.springboot.fridge.dto.CreateItemRequest;
import gr2536.springboot.fridge.dto.RemoveResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Validated
@RequiredArgsConstructor
public class FridgeController {

  private final FridgeService svc;
  private static final Logger log = Logger.getLogger(FridgeController.class.getName());

  @GetMapping
  public List<Item> list() {
    return svc.list();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Item add(@Valid @RequestBody CreateItemRequest req) {
    return svc.add(req);
  }

  @DeleteMapping("/{name}")
  public ResponseEntity<RemoveResult> remove(@PathVariable String name, @RequestParam("qty") @Positive int qty) {
    int remaining = svc.remove(name, qty);
    return ResponseEntity.ok(new RemoveResult(name, remaining));
  }

  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public List<Item> replace(@RequestBody List<Item> items) {
      log.info(String.format("SYNC: Replacing server fridge with %d items from UI.", items == null ? 0 : items.size()));
      svc.replace(items);
      return svc.list();
  }
}
