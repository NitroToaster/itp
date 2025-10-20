import React, { useEffect, useState } from 'react';
import './styles/App.css';
import FridgePage from './pages/Fridge';
import ShoppingListPage from './pages/ShoppingList';

// fridge API
import { fetchItems, addItemApi, removeItemApi } from './config/FridgeService';
// shopping API (ensure file is src/config/ShoppingListService.js)
import {
  fetchShoppingItems,
  markBought,
  addShoppingItem,
  removeShoppingItem,
} from './config/ShoppingListService';

function App() {
  const [view, setView] = useState('fridge'); // 'fridge' | 'shopping'

  // fridge state
  const [fridgeItems, setFridgeItems] = useState([]);
  const [fridgeLoading, setFridgeLoading] = useState(false);

  // shopping state
  const [shoppingItems, setShoppingItems] = useState([]);
  const [shoppingLoading, setShoppingLoading] = useState(false);

  async function loadFridgeItems() {
    setFridgeLoading(true);
    const data = await fetchItems();
    setFridgeItems(data || []);
    setFridgeLoading(false);
  }

  async function loadShoppingItems() {
    setShoppingLoading(true);
    const data = await fetchShoppingItems();
    setShoppingItems(data || []);
    setShoppingLoading(false);
  }

  useEffect(() => {
    // load both sets on start
    loadFridgeItems();
    loadShoppingItems();
  }, []);

  // fridge handlers
  const handleAdd = async item => {
    try {
      await addItemApi(item);
      await loadFridgeItems();
    } catch (e) {
      console.error('Failed to add fridge item', e);
    }
  };

  const handleRemove = async id => {
    try {
      await removeItemApi(id);
      setFridgeItems(prev => prev.filter(i => i.id !== id));
    } catch (e) {
      console.error('Failed to remove fridge item', e);
      await loadFridgeItems();
    }
  };

  // shopping handlers
  const handleAddShopping = async item => {
    try {
      await addShoppingItem(item);
      await loadShoppingItems();
    } catch (e) {
      console.error('Failed to add shopping item', e);
    }
  };

  const handleRemoveShopping = async id => {
    try {
      await removeShoppingItem(id);
      setShoppingItems(prev => prev.filter(i => i.id !== id));
    } catch (e) {
      console.error('Failed to remove shopping item', e);
      await loadShoppingItems();
    }
  };

  const handleMarkBought = async id => {
    try {
      await markBought(id);
      // refresh both lists: shopping list lost an item, fridge gained one
      await Promise.all([loadShoppingItems(), loadFridgeItems()]);
    } catch (e) {
      console.error('Failed to mark bought', e);
    }
  };

  return (
    <div className="App">
      <nav className="top-nav">
        <button onClick={() => setView('fridge')}>Fridge</button>
        <button onClick={() => setView('shopping')}>Shopping List</button>
      </nav>

      <main>
        {view === 'fridge' && (
          <FridgePage
            items={fridgeItems}
            loading={fridgeLoading}
            onAdd={handleAdd}
            onRemove={handleRemove}
            reload={loadFridgeItems}
          />
        )}

        {view === 'shopping' && (
          <ShoppingListPage
            items={shoppingItems}
            loading={shoppingLoading}
            onRemove={handleRemoveShopping}
            onAdd={handleAddShopping}
            onMarkBought={handleMarkBought}
            reload={loadShoppingItems}
          />
        )}
      </main>
    </div>
  );
}

export default App;
