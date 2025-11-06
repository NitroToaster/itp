import { api } from './api';

// Backend <-> frontend field mapping helpers
function backendToFrontend(it, idx) {
  // backend item: { name, quantity, expirationDate }
  return {
    id: it.id ?? it.name ?? `item-${idx}-${String(Math.random()).slice(2)}`,
    name: it.name,
    qty: it.quantity ?? 0,
    expiration: it.expirationDate ?? null
  };
}
function frontendToBackend(it) {
  // frontend item: { id, name, qty, expiration }
  return {
    name: it.name,
    quantity: it.qty ?? 0,
    expirationDate: it.expiration ?? null
  };
}

/**
 * Fetch items from backend inventory.
 * Backend may return { items: [...] } or an array. Normalize to frontend shape.
 */
export async function fetchItems() {
  try {
    const res = await api.get('/inventory');
    const payload = res.data ?? [];
    const list = Array.isArray(payload) ? payload : (payload.items ?? payload.content ?? []);
    return (list || []).map((it, idx) => backendToFrontend(it, idx));
  } catch (e) {
    console.error('fetchItems error', e);
    return [];
  }
}

/**
 * Add single item to backend.
 * Returns created item in frontend shape (best-effort).
 */
export async function addItemApi(item) {
  try {
    const body = frontendToBackend(item);
    const res = await api.post('/inventory', body);
    // backend might return the created item or full inventory; try to normalize
    const created = res.data;
    if (!created) return frontendToBackend(item);
    if (Array.isArray(created)) {
      // returned full inventory -> find by name
      const found = created.find(i => i.name === body.name) ?? created[created.length - 1];
      return backendToFrontend(found);
    }
    if (created.name) return backendToFrontend(created);
    return frontendToBackend(item);
  } catch (e) {
    console.error('addItemApi error', e);
    throw e;
  }
}

/**
 * Update an item by id (frontend id). Backend doesn't expose per-id PUT,
 * so fetch current inventory, apply change and PUT entire list (replace).
 */
export async function updateItemApi(id, item) {
  try {
    // fetch current inventory from backend
    const current = await fetchItems();
    // find index by id or name fallback
    const index = current.findIndex(i => String(i.id) === String(id) || i.name === item.name);
    if (index === -1) {
      throw new Error('Item not found');
    }
    // build new backend list by mapping existing backend items and replacing target
    // fetch raw backend representation first to preserve fields
    const res = await api.get('/inventory');
    const payload = res.data ?? [];
    const backendList = Array.isArray(payload) ? payload : (payload.items ?? payload.content ?? []);
    // find backend index by name match (best-effort)
    const targetName = current[index].name;
    const backendIdx = backendList.findIndex(b => b.name === targetName);
    const updatedBackendItem = frontendToBackend(item);
    if (backendIdx !== -1) {
      backendList[backendIdx] = updatedBackendItem;
    } else {
      // fallback: replace by name or append
      const replaced = backendList.map(b => (b.name === targetName ? updatedBackendItem : b));
      if (JSON.stringify(replaced) === JSON.stringify(backendList)) {
        backendList.push(updatedBackendItem);
      } else {
        backendList.splice(0, backendList.length, ...replaced);
      }
    }
    // Replace full inventory on backend
    await api.put('/inventory', backendList);
    // Return frontend-mapped updated item
    return item;
  } catch (e) {
    console.error('updateItemApi error', e);
    throw e;
  }
}

/**
 * Remove an item. Frontend calls with id; map to name and call DELETE /inventory/{name}?qty=...
 */
export async function removeItemApi(itemId, qty = 1) {
  try {
    // resolve name by fetching inventory
    const items = await fetchItems();
    const found = items.find(i => String(i.id) === String(itemId));
    const name = found ? found.name : itemId; // if caller passed name already
    if (!name) throw new Error('Unable to resolve item name for delete');
    const res = await api.delete(`/inventory/${encodeURIComponent(name)}`, { params: { qty } });
    return res.data;
  } catch (e) {
    console.error('removeItemApi error', e);
    throw e;
  }
}