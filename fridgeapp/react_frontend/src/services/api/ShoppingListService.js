import { api } from './api';

// Backend <-> frontend field mapping helpers
function backendToFrontend(it, idx) {
  return {
    id: it.id ?? it.name ?? `shopping-list-${idx}-${String(Math.random()).slice(2)}`,
    name: it.name,
    qty: it.quantity ?? 0
  };
}

function frontendToBackend(it) {
  return {
    name: it.name,
    quantity: it.qty ?? 0
  };
}

export async function fetchShoppingItems() {
  try {
    const res = await api.get('/shopping-list/items'); // FIXED: added /items
    const payload = res.data ?? [];
    const list = Array.isArray(payload) ? payload : (payload.items ?? payload.content ?? []);
    return (list || []).map((it, idx) => backendToFrontend(it, idx));
  } catch (e) {
    console.error('fetchShoppingItems error', e);
    return [];
  }
}

export async function addShoppingItem(item) {
  try {
    const body = frontendToBackend(item);
    const res = await api.post('/shopping-list/items', body);
    const created = res.data;
    if (!created) return backendToFrontend(item);
    return backendToFrontend(created);
  } catch (e) {
    console.error('addShoppingItem error', e);
    throw e;
  }
}

export async function removeShoppingItem(itemId) {
  try {
    const items = await fetchShoppingItems();
    const found = items.find(i => String(i.id) === String(itemId));
    const name = found ? found.name : itemId;
    if (!name) throw new Error('Unable to resolve item name for delete');
    const res = await api.delete(`/shopping-list/items/${encodeURIComponent(name)}`, { 
      params: { qty: found?.qty || 1 } 
    });
    return res.data;
  } catch (e) {
    console.error('removeShoppingItem error', e);
    throw e;
  }
}

export async function markBought(itemId) {
  try {
    const items = await fetchShoppingItems();
    const found = items.find(i => String(i.id) === String(itemId));
    if (!found) throw new Error('Item not found');
    
    // Move to fridge (using same shape as fridge items)
    const fridgeItem = {
      name: found.name,
      quantity: found.qty,
      expirationDate: null
    };
    
    // Add to fridge first, then remove from shopping
    await api.post('/inventory', fridgeItem);
    await removeShoppingItem(itemId);
    
    return true;
  } catch (e) {
    console.error('markBought error', e);
    throw e;
  }
}

// For adding recipe ingredients - FIXED: add items one by one
export async function addRecipeItemsToShopping(items) {
  try {
    const results = [];
    for (const item of items) {
      const result = await addShoppingItem(item);
      results.push(result);
    }
    return results;
  } catch (e) {
    console.error('addRecipeItemsToShopping error', e);
    throw e;
  }
}