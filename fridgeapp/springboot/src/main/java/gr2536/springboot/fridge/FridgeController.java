package gr2536.springboot.fridge;

import gr2536.core.Item;
import gr2536.springboot.fridge.dto.CreateItemRequest;
import gr2536.springboot.fridge.dto.RemoveResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Validated
@RequiredArgsConstructor
public class FridgeController {

  private final FridgeService svc;

  @GetMapping
  public List<Item> list() { return svc.list(); }

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
}
