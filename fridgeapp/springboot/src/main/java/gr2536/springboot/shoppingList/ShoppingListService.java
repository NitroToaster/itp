package gr2536.springboot.shoppingList;

import gr2536.core.fridge.Fridge;
import gr2536.core.item.Item;
import gr2536.core.shoppingList.ShoppingList;
import gr2536.springboot.fridge.dto.CreateItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

  private final ShoppingList list;
  private final Fridge fridge;

  public List<Item> list() {
    return list.listItems();
  }

  public Item add(CreateItemRequest req) {
    var it = new Item(req.name().trim(), req.quantity(), req.expirationDate());
    list.add(it);
    return it;
  }

  public int remove(String name, int qty) {
    return list.remove(name, qty);
  }

  public void moveAllToFridge(boolean clearAfter) {
    if (clearAfter) {
      list.addToFridge(fridge);
      return;
    }

    // Copy items to the fridge but keep them on the shopping list when clearAfter is false.
    list.listItems().forEach(fridge::add);
  }
}
