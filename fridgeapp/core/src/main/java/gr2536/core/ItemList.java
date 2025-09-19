package gr2536.core;

import java.util.*;

interface ItemList {

    void add(Item item);

    int remove(String name, int quantityToRemove);

    int getQuantity(String name);

    List<Item> listItems();
}