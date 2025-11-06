import React, { useEffect, useState } from 'react';
import './assets/styles/App.css';
import FridgePage from './features/fridge/Fridge';
import ShoppingListPage from './features/shopping/ShoppingList';
import AppShell from './components/layout/AppShell';
import RecipesPage from './features/recipes/RecipesPage';

// fridge API
import { fetchItems, addItemApi, removeItemApi, updateItemApi } from './services/api/FridgeService';
// shopping API
import {
  fetchShoppingItems,
  markBought,
  addShoppingItem,
  removeShoppingItem,
} from './services/api/ShoppingListService';

function App() {
  const [view, setView] = useState('fridge');

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
    loadFridgeItems();
    loadShoppingItems();
  }, []);

  // Handle data import - reload both lists
  const handleDataLoaded = async () => {
    await loadFridgeItems();
    await loadShoppingItems();
  };

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

  const handleUpdate = async item => {
    try {
      await updateItemApi(item.id, item);
      await loadFridgeItems();
    } catch (e) {
      console.error('Failed to update fridge item', e);
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
      await Promise.all([loadShoppingItems(), loadFridgeItems()]);
    } catch (e) {
      console.error('Failed to mark bought', e);
    }
  };

  return (
    <AppShell onNavigate={setView} onDataLoaded={handleDataLoaded}>
      {view === 'fridge' && (
        <FridgePage
          items={fridgeItems}
          loading={fridgeLoading}
          onAdd={handleAdd}
          onRemove={handleRemove}
          onUpdate={handleUpdate}
          reload={loadFridgeItems}
          onOpenShopping={() => setView('shopping')}
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
      {view === 'recipes' && (
      <RecipesPage
        onAddToShopping={handleAddShopping}
      />
    )}
    </AppShell>
  );
}

export default App;