# Services Documentation

## Overview

The services layer handles all data operations in FridgeApp. It provides an abstraction between UI components and data storage, supporting both mock (localStorage) and real API modes.

## Structure

```
services/
├── api/
│   ├── fridgeService.js
│   └── shoppingService.js
└── dataService.js
```

## Fridge Service

**Location:** `src/services/api/fridgeService.js`

### Functions

**fetchItems()**
- Retrieves all fridge items
- Returns array of items

**addItemApi(item)**
- Adds new item to fridge
- Parameters: name (required), qty, expiration
- Returns the created item with ID

**updateItemApi(id, item)**
- Updates existing item
- Parameters: id, updated item object
- Returns updated item

**removeItemApi(id)**
- Removes item from fridge
- Parameters: item id
- Returns success status

### Usage Example

```javascript
// Add item
const newItem = await addItemApi({
  name: "Milk",
  qty: 2,
  expiration: "2025-10-30"
});

// Update item
await updateItemApi(1, {
  name: "Whole Milk",
  qty: 3
});

// Remove item
await removeItemApi(1);
```

## Shopping Service

**Location:** `src/services/api/shoppingService.js`

### Functions

**fetchShoppingItems()**
- Retrieves all shopping list items
- Returns array of items

**addShoppingItem(item)**
- Adds item to shopping list
- Parameters: name (required), qty
- Returns created item

**removeShoppingItem(id)**
- Removes item from shopping list
- Parameters: item id
- Returns success status

**markBought(id)**
- Moves item from shopping list to fridge
- Parameters: item id
- Removes from shopping list and adds to fridge

## Data Service

**Location:** `src/services/dataService.js`

### Functions

**loadDataFromFile(file)**
- Imports data from JSON file
- Parameters: File object from input
- Validates structure before importing
- Returns success status and message

**validateData(data)**
- Validates imported data structure
- Checks for required fields
- Throws errors if validation fails

**exportDataToFile()** (planned)
- Exports current data to JSON file
- Not yet implemented

### JSON Format

```json
{
  "fridgeItems": [
    { "name": "Milk", "qty": 2, "expiration": "2025-10-30" }
  ],
  "shoppingItems": [
    { "name": "Bread", "qty": 1 }
  ]
}
```

## Mock API System

### Overview

The mock system uses localStorage to simulate API calls during development.

### Mode Detection

```javascript
const useMock = !api.defaults.baseURL || api.defaults.baseURL === 'mock';
```

If `useMock` is true, all operations use localStorage. Otherwise, real API calls are made.

### localStorage Keys

- `fridge_mock_store_v1` - Fridge items
- `shopping_mock_store_v1` - Shopping items

### Switching to Real API

To use a real backend:

1. Set API base URL in `services/api/api.js`:
```javascript
export const api = axios.create({
  baseURL: 'https://your-api.com/api'
});
```

2. Services automatically switch to API mode
3. Update endpoint paths if needed

## Error Handling

All service calls should be wrapped in try-catch blocks:

```javascript
try {
  await addItemApi(item);
  await loadFridgeItems();
} catch (error) {
  console.error('Failed to add item:', error);
  alert('Failed to add item');
}
```

### Common Errors

**localStorage full:**
- Occurs when browser storage quota exceeded
- Clear old data or use smaller datasets

**Invalid JSON:**
- File is not valid JSON format
- Check file structure before importing

**Network errors (API mode):**
- Backend unavailable
- Check connection and API status

## Helper Functions

**delay(ms)**
- Simulates network latency in mock mode
- Used to make mock API feel more realistic

**loadStore()**
- Reads data from localStorage
- Returns parsed array or empty array

**saveStore(items)**
- Writes data to localStorage
- Handles errors silently

## Development Notes

### Debugging

View localStorage data in browser console:
```javascript
console.log(localStorage.getItem('fridge_mock_store_v1'));
```

Clear all data:
```javascript
localStorage.clear();
window.location.reload();
```

### Adding New Functions

1. Add function to appropriate service file
2. Implement both mock and API modes
3. Add error handling
4. Update App.jsx to use new function
5. Pass function to components via props