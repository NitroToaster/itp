package gr2536.springboot.fridge;

import gr2536.core.fridge.Fridge;
import gr2536.core.item.Item;
import gr2536.springboot.fridge.dto.CreateItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FridgeService {
  private final Fridge fridge;

  public List<Item> list() {
    return fridge.listItems();
  }

  public Item add(CreateItemRequest req) {
    var it = new Item(req.name().trim(), req.quantity(), req.expirationDate());
    fridge.add(it); // persists via FileManager
    return it;
  }

  public int remove(String name, int qty) {
    return fridge.remove(name, qty);
  }
}
