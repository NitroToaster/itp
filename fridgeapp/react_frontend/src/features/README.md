# Features Documentation

## Fridge Management

### Adding Items

**Location:** Fridge Overview → Add Item button

**Fields:**
- **Name** (required) - Item name
- **Quantity** (optional) - Number of items (default: 1)
- **Expiration Date** (optional) - Date in YYYY-MM-DD format

**Steps:**
1. Click "Add Item" button
2. Fill in the form
3. Click "Add Item" to save

### Editing Items

**Location:** Fridge Overview → Hover over item → Edit button

**Steps:**
1. Hover over any item
2. Click "Edit" button
3. Modify fields
4. Click "Save"

### Removing Items

**Location:** Fridge Overview → Hover over item → Remove button

Clicking "Remove" immediately deletes the item.

### Expiration Color System

Items display a colored dot indicating expiration status:

| Color | Days Remaining | Status |
|-------|----------------|--------|
| Green | 5+ days | Fresh |
| Yellow | 2-5 days | Use soon |
| Orange | 0-2 days | Expiring |
| Red | Expired | Do not use |
| Gray | No date set | Unknown |

Hover over the dot to see the exact status.

### Search and Filter

**Search:** Type in the search bar to filter by name (case-insensitive)

**Sort:** Choose from dropdown menu
- Default
- Name (A-Z)
- Expiration (soonest first)
- Quantity (lowest first)

**Filters:** Click "Filter" button to show advanced options
- Min/Max Quantity
- Expiration date range

Click "Apply Filters" to apply, "Clear Filters" to reset.

## Shopping List

### Adding Items

**Location:** Shopping List → Add Item button

**Fields:**
- **Name** (required)
- **Quantity** (optional, default: 1)

Shopping items do not have expiration dates until moved to fridge.

### Selecting Items

Click on any item card to select/deselect it. Selected items show:
- Cyan border
- Checked checkbox

### Searching

Type in the search bar to filter items. Search matches from the beginning of item names only.

**Examples:**
- "mi" finds "Milk"
- "ilk" does not find "Milk"

### Bulk Actions

**Add All to Inventory:**
- Moves all shopping items to fridge
- Clears shopping list
- Items added without expiration dates

**Remove All:**
- Deletes all items from shopping list
- No confirmation prompt

**Remove Selected:**
- Deletes only selected items
- Requires at least one item selected

### Individual Actions

Hover over any item to reveal the "Remove" button for that item.

## Data Import

### Importing Data

**Location:** Sidebar → Load Data button

**Steps:**
1. Click "Load Data"
2. Select a JSON file from your computer
3. Data is validated and imported
4. Success message shows import summary

### File Format

Required JSON structure:

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

**Validation Rules:**
- Must be valid JSON
- Arrays for fridgeItems and shoppingItems
- Each item requires a name field
- qty must be a number (if present)
- Missing IDs are auto-generated

### Error Messages

- "Invalid JSON file" - File is not valid JSON
- "Missing name" - Item lacks required name field
- "Must be an array" - Items are not in array format

## Navigation

### Sidebar

**Fridge Overview** - View and manage fridge items
**Shopping List** - View and manage shopping list
**Load Data** - Import data from JSON file

### Collapsible Sidebar

Click the toggle button (☰ / ‹) to collapse/expand the sidebar. When collapsed, only icons are visible.