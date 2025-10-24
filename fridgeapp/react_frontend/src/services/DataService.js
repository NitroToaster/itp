// DataService.js - Handles import/export of data

const FRIDGE_LS_KEY = 'fridge_mock_store_v1';
const SHOPPING_LS_KEY = 'shopping_mock_store_v1';

/**
 * Validates that the data has the correct structure
 */
function validateData(data) {
  if (!data || typeof data !== 'object') {
    throw new Error('Invalid data format: Expected an object');
  }

  // Validate fridge items if present
  if (data.fridgeItems !== undefined) {
    if (!Array.isArray(data.fridgeItems)) {
      throw new Error('Invalid data format: fridgeItems must be an array');
    }
    
    // Validate each item has required fields
    data.fridgeItems.forEach((item, index) => {
      if (!item.name) {
        throw new Error(`Invalid fridge item at index ${index}: Missing name`);
      }
      if (item.qty !== undefined && typeof item.qty !== 'number') {
        throw new Error(`Invalid fridge item at index ${index}: qty must be a number`);
      }
    });
  }

  // Validate shopping items if present
  if (data.shoppingItems !== undefined) {
    if (!Array.isArray(data.shoppingItems)) {
      throw new Error('Invalid data format: shoppingItems must be an array');
    }
    
    data.shoppingItems.forEach((item, index) => {
      if (!item.name) {
        throw new Error(`Invalid shopping item at index ${index}: Missing name`);
      }
      if (item.qty !== undefined && typeof item.qty !== 'number') {
        throw new Error(`Invalid shopping item at index ${index}: qty must be a number`);
      }
    });
  }

  return true;
}

/**
 * Loads data from a JSON file
 */
export async function loadDataFromFile(file) {
  try {
    const text = await file.text();
    const data = JSON.parse(text);
    
    // Validate the data structure
    validateData(data);
    
    // Import fridge items
    if (data.fridgeItems && Array.isArray(data.fridgeItems)) {
      // Ensure each item has an ID
      const itemsWithIds = data.fridgeItems.map(item => ({
        ...item,
        id: item.id || Date.now() + Math.random(),
        qty: item.qty || 1,
      }));
      
      localStorage.setItem(FRIDGE_LS_KEY, JSON.stringify(itemsWithIds));
    }
    
    // Import shopping items
    if (data.shoppingItems && Array.isArray(data.shoppingItems)) {
      const itemsWithIds = data.shoppingItems.map(item => ({
        ...item,
        id: item.id || Date.now() + Math.random(),
        qty: item.qty || 1,
      }));
      
      localStorage.setItem(SHOPPING_LS_KEY, JSON.stringify(itemsWithIds));
    }
    
    return {
      success: true,
      message: `Successfully imported ${data.fridgeItems?.length || 0} fridge items and ${data.shoppingItems?.length || 0} shopping items`,
      data: {
        fridgeItemsCount: data.fridgeItems?.length || 0,
        shoppingItemsCount: data.shoppingItems?.length || 0,
      }
    };
    
  } catch (error) {
    console.error('Error loading data:', error);
    
    if (error instanceof SyntaxError) {
      throw new Error('Invalid JSON file. Please check the file format.');
    }
    
    throw error;
  }
}

/**
 * Exports current data to a JSON file
 */
export function exportDataToFile() {
  try {
    // Get current data from localStorage
    const fridgeData = localStorage.getItem(FRIDGE_LS_KEY);
    const shoppingData = localStorage.getItem(SHOPPING_LS_KEY);
    
    const data = {
      fridgeItems: fridgeData ? JSON.parse(fridgeData) : [],
      shoppingItems: shoppingData ? JSON.parse(shoppingData) : [],
      exportedAt: new Date().toISOString(),
    };
    
    // Create blob and download
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `fridgeapp-data-${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    return {
      success: true,
      message: 'Data exported successfully'
    };
    
  } catch (error) {
    console.error('Error exporting data:', error);
    throw new Error('Failed to export data');
  }
}

/**
 * Clears all data (for testing purposes)
 */
export function clearAllData() {
  try {
    localStorage.removeItem(FRIDGE_LS_KEY);
    localStorage.removeItem(SHOPPING_LS_KEY);
    
    return {
      success: true,
      message: 'All data cleared successfully'
    };
  } catch (error) {
    console.error('Error clearing data:', error);
    throw new Error('Failed to clear data');
  }
}