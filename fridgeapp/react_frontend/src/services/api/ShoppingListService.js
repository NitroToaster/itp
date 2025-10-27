import { api } from './api';
import { addItemApi as addFridgeItemApi } from './FridgeService'; // used when marking bought

// simulated latency helper
const delay = ms => new Promise(res => setTimeout(res, ms));

// shopping mock store
const LS_KEY = 'shopping_mock_store_v1';
const seed = [
  { id: 1, name: 'Butter', qty: 1, note: 'Salted' },
  { id: 2, name: 'Tomatoes', qty: 4, note: '' }
];

function loadStore() {
  try {
    const raw = localStorage.getItem(LS_KEY);
    return raw ? JSON.parse(raw) : seed.slice();
  } catch {
    return seed.slice();
  }
}
function saveStore(items) {
  try { localStorage.setItem(LS_KEY, JSON.stringify(items)); } catch {}
}

// detect mock mode (same detection as FridgeService)
const useMock = !api.defaults.baseURL || api.defaults.baseURL === 'mock';

/**
 * Fetch shopping list items.
 * Backend: GET /shopping
 * Mock: returns localStorage list
 */
export async function fetchShoppingItems() {
  if (useMock) {
    await delay(160);
    return loadStore();
  }
  try {
    const res = await api.get('/shopping');
    return res.data?.content ?? res.data;
  } catch (e) {
    console.error('fetchShoppingItems error', e);
    return [];
  }
}

/**
 * Add an item to the shopping list.
 * Backend: POST /shopping
 * Mock: persist to localStorage
 */
export async function addShoppingItem(item) {
  if (useMock) {
    await delay(120);
    const items = loadStore();
    const id = Date.now();
    const newItem = { id, ...item };
    items.push(newItem);
    saveStore(items);
    return newItem;
  }
  try {
    const res = await api.post('/shopping', item);
    return res.data;
  } catch (e) {
    console.error('addShoppingItem error', e);
    throw e;
  }
}

/**
 * Remove item from shopping list.
 * Backend: DELETE /shopping/{id}
 * Mock: remove from localStorage
 */
export async function removeShoppingItem(id) {
  if (useMock) {
    await delay(120);
    let items = loadStore();
    items = items.filter(i => i.id !== id);
    saveStore(items);
    return { ok: true };
  }
  try {
    const res = await api.delete(`/shopping/${id}`);
    return res.data;
  } catch (e) {
    console.error('removeShoppingItem error', e);
    throw e;
  }
}

/**
 * Mark shopping item as bought:
 * - Backend: POST /shopping/{id}/bought (or similar) — server can move/add to fridge
 * - Mock: remove from shopping list and add to fridge using FridgeService.addItemApi
 *
 * Returns whatever backend/mock returns.
 */
export async function markBought(id) {
  if (useMock) {
    await delay(160);
    const items = loadStore();
    const idx = items.findIndex(i => i.id === id);
    if (idx === -1) return { ok: false };
    const item = items[idx];
    // remove from shopping list
    items.splice(idx, 1);
    saveStore(items);
    // add to fridge (mock fridge service will create a new fridge entry)
    try {
      await addFridgeItemApi({ name: item.name, qty: item.qty ?? 1, expiration: item.expiration ?? null });
    } catch (e) {
      console.warn('Failed to add to fridge (mock)', e);
    }
    return { ok: true, moved: item };
  }
  try {
    const res = await api.post(`/shopping/${id}/bought`);
    return res.data;
  } catch (e) {
    console.error('markBought error', e);
    throw e;
  }
}