import { api } from './api';

// simple helper for simulated latency
const delay = ms => new Promise(res => setTimeout(res, ms));

// mock store key & seed
const LS_KEY = 'fridge_mock_store_v1';
const seed = [
  { id: 1, name: 'Milk', qty: 1, expiration: '2025-10-14' },
  { id: 2, name: 'Eggs', qty: 12, expiration: '2025-10-20' }
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

// detect mock mode
const useMock = !api.defaults.baseURL || api.defaults.baseURL === 'mock';

export async function fetchItems() {
  if (useMock) {
    await delay(200);
    return loadStore();
  }
  try {
    const res = await api.get('/items');
    // adapt to backend shape if needed (res.data or res.data.content)
    return res.data?.content ?? res.data;
  } catch (e) {
    console.error('fetchItems error', e);
    return [];
  }
}

export async function addItemApi(item) {
  if (useMock) {
    await delay(200);
    const items = loadStore();
    const id = Date.now();
    const newItem = { id, ...item };
    items.push(newItem);
    saveStore(items);
    return newItem;
  }
  try {
    const res = await api.post('/items', item);
    return res.data;
  } catch (e) {
    console.error('addItemApi error', e);
    throw e;
  }
}

export async function removeItemApi(id) {
  if (useMock) {
    await delay(150);
    let items = loadStore();
    items = items.filter(i => i.id !== id);
    saveStore(items);
    return { ok: true };
  }
  try {
    const res = await api.delete(`/items/${id}`);
    return res.data;
  } catch (e) {
    console.error('removeItemApi error', e);
    throw e;
  }
}