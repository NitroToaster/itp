package gr2536.springboot.shoppingList;

import gr2536.core.Item;
import gr2536.core.ShoppingList;
import gr2536.core.Fridge;
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
    list.addToFridge(fridge);
    if (!clearAfter) {
      
    }
  }
}
