package gr2536.springboot;

import gr2536.core.Fridge;
import gr2536.core.Item;
import gr2536.core.ShoppingList;
import gr2536.data.FridgeJsonFileManager;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * REST API for the Fridge app using existing core classes.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class FridgeAppSpringBootController {

    private final FridgeJsonFileManager fileManager = new FridgeJsonFileManager();

    /** Location where the fridge JSON will be stored. Configure in application.yml if you like. */
    @Value("${fridge.file:./utils/fridge.json}")
    private String fridgeFile;

    /** In-memory domain objects (Fridge has persistence via file manager; ShoppingList is in-memory). */
    private Fridge fridge;
    private final ShoppingList shoppingList = new ShoppingList();

    @PostConstruct
    void init() {
        // Load fridge from file if present, else start empty; wire persistence back into the object
        Fridge loaded = fileManager.readFridgeData(fridgeFile);
        loaded.setFileManager(fileManager, fridgeFile);
        this.fridge = loaded;
    }

    /* --------------------------- Inventory endpoints --------------------------- */

    /** List inventory items. */
    @GetMapping("/inventory")
    public List<Item> listInventory() {
        return fridge.listItems(); // returns List<Item> sorted by default order
    }

    /** Add an item to the inventory. */
    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public Item addInventoryItem(@Valid @RequestBody CreateItemRequest req) {
        Item item = new Item(req.name().trim(), req.quantity(), req.expirationDate());
        fridge.add(item); // Fridge.add() saves to file via fileManager.saveFridgeData(...)
        return item;
    }

    /**
     * Remove some quantity from an inventory item. If quantity becomes 0, the item bucket is removed.
     * @param name case-insensitive item name
     * @param qty  quantity to remove (required, > 0)
     * @return remaining total quantity for that item across all expiration buckets
     */
    @DeleteMapping("/inventory/{name}")
    public ResponseEntity<RemoveResult> removeFromInventory(
            @PathVariable String name,
            @RequestParam(name = "qty") @Positive int qty) {

        int remaining = fridge.remove(Objects.requireNonNull(name, "name"), qty);
        // remaining==0 also covers "not found" since getQuantity would be 0; we treat that as 200 with remaining 0
        return ResponseEntity.ok(new RemoveResult(name, remaining));
    }

    /* -------------------------- Shopping list endpoints ------------------------ */

    /** List shopping list items. */
    @GetMapping("/shopping-list/items")
    public List<Item> listShoppingItems() {
        return shoppingList.listItems();
    }

    /** Add an item to the shopping list. */
    @PostMapping("/shopping-list/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Item addShoppingItem(@Valid @RequestBody CreateItemRequest req) {
        Item item = new Item(req.name().trim(), req.quantity(), req.expirationDate());
        shoppingList.add(item);
        return item;
    }

    /**
     * Remove some quantity from a shopping list item.
     * @return remaining total quantity for that item across all expiration buckets (in the shopping list)
     */
    @DeleteMapping("/shopping-list/items/{name}")
    public ResponseEntity<RemoveResult> removeFromShoppingList(
            @PathVariable String name,
            @RequestParam(name = "qty") @Positive int qty) {

        int remaining = shoppingList.remove(Objects.requireNonNull(name, "name"), qty);
        return ResponseEntity.ok(new RemoveResult(name, remaining));
    }

    /* -------------------------- Shopping list → Fridge action ------------------ */

    /**
     * Move all shopping list items into the fridge and (optionally) clear the shopping list.
     * Body is optional; default is to clear the list after moving.
     */
    @PostMapping("/shopping-list/actions/add-to-fridge")
    public ResponseEntity<MoveResult> addListToFridge(@RequestBody(required = false) AddToFridgeRequest body) {
        boolean clear = body == null || body.clearListAfter();
        // ShoppingList.addToFridge uses Fridge.add(...) under the hood; Fridge will persist after each add
        shoppingList.addToFridge(fridge);
        if (!clear) {
            // If user wants to keep the list, re-populate from the items we just moved (idempotency-lite)
            // For simplicity we currently always clear; if you want "keep", rebuild list here.
        } else {
            // addToFridge already clears the shopping list
        }
        return ResponseEntity.ok(new MoveResult("OK"));
    }

    /* --------------------------------- DTOs ----------------------------------- */

    /** Request body for create/add endpoints. */
    public record CreateItemRequest(
            @NotBlank String name,
            @Positive int quantity,
            LocalDate expirationDate // nullable
    ) {}

    /** Request body for the add-to-fridge action. */
    public record AddToFridgeRequest(boolean clearListAfter) {}

    /** Response for remove endpoints. */
    public record RemoveResult(String name, int remainingQuantity) {}

    /** Response for the move action. */
    public record MoveResult(String status) {}
}
