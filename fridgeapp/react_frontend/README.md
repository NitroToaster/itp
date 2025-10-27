# FridgeApp - React Frontend

## App Description

FridgeApp is a web application for managing refrigerator inventory and shopping lists. Users can track items with expiration dates, create shopping lists, and import/export data for backup purposes.

## Project Structure

### Assets
The **assets** folder contains static files like stylesheets.

- **styles/App.css** - Global styles and theme configuration

### Components
The **components** folder contains reusable UI components organized by purpose.

#### Layout
- **AppShell.jsx** - Main layout wrapper containing sidebar and content area
- **Sidebar.jsx** - Navigation sidebar with collapse functionality

#### Modals
- **AddItemModal.jsx** - Modal for adding new fridge items
- **EditItemModal.jsx** - Modal for editing existing fridge items
- **AddShoppingItemModal.jsx** - Modal for adding shopping list items

### Features
The **features** folder contains feature-specific page components.

#### Fridge
- **FridgePage.jsx** - Main fridge interface with search, filter, and item management

#### Shopping
- **ShoppingListPage.jsx** - Shopping list interface with bulk operations

### Services
The **services** folder contains all data handling logic.

#### Api
- **fridgeService.js** - CRUD operations for fridge items
- **shoppingService.js** - CRUD operations for shopping list items

- **dataService.js** - Import and export functionality for data backup

### More Information
See [docs/FEATURES.md](docs/FEATURES.md) for detailed feature documentation.
See [docs/SERVICES.md](docs/SERVICES.md) for service layer documentation.

## Technologies and Dependencies

- **React:** 18
- **Node.js:** 14 or higher
- **npm:** 6.x or higher

## Features

- Add, edit, and remove fridge items
- Track expiration dates with color-coded indicators
- Search and filter items
- Create and manage shopping lists
- Import data from JSON files
- Data persistence using localStorage

## How to Run the Project

```bash
cd react_frontend
npm install
npm start
```

The application will open at http://localhost:3000

## Data Storage

The application uses browser localStorage for data persistence:

- `fridge_mock_store_v1` - Fridge items
- `shopping_mock_store_v1` - Shopping list items

Data persists across browser sessions but will be cleared if browser data is cleared.

## Import Data Format

To import data, use the following JSON structure:

```json
{
  "fridgeItems": [
    {
      "id": 1,
      "name": "Milk",
      "qty": 2,
      "expiration": "2025-10-30"
    }
  ],
  "shoppingItems": [
    {
      "id": 1,
      "name": "Bread",
      "qty": 1
    }
  ]
}
```

Click "Load Data" in the sidebar to import a JSON file.
