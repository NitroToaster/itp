# Components Documentation

## Component Hierarchy

```
App.jsx
└── AppShell
    ├── Sidebar
    └── Fridge OR ShoppingList
        ├── AddItemModal
        ├── EditItemModal
        └── AddShoppingItemModal
```

## Layout Components

### AppShell

**Location:** `src/components/layout/AppShell.jsx`

Main layout wrapper containing sidebar and content area.

**Props:**
- `children` - Content to display
- `onNavigate` - Navigation callback
- `onDataLoaded` - Data import callback

---

### Sidebar

**Location:** `src/components/layout/Sidebar.jsx`

Navigation sidebar with route switching and data import.

**Props:**
- `collapsed` - Sidebar collapse state
- `onToggle` - Toggle collapse callback
- `onNavigate` - Navigation callback
- `onDataLoaded` - Data import callback

---

## Page Components

### Fridge

**Location:** `src/features/fridge/Fridge.jsx`

Main fridge management interface.

**Props:**
- `items` - Array of fridge items
- `loading` - Loading state
- `onAdd` - Add item callback
- `onRemove` - Remove item callback
- `onUpdate` - Update item callback
- `reload` - Refresh data callback

**Features:**
- Search and filter
- Sort by name, expiration, or quantity
- Color-coded expiration indicators
- Add, edit, remove items

---

### ShoppingList

**Location:** `src/features/shopping/ShoppingList.jsx`

Shopping list management interface.

**Props:**
- `items` - Array of shopping items
- `loading` - Loading state
- `onRemove` - Remove item callback
- `onAdd` - Add item callback
- `onMarkBought` - Mark bought callback
- `reload` - Refresh data callback

**Features:**
- Prefix-based search
- Multi-select items
- Bulk operations

---

## Modal Components

### AddItemModal

**Location:** `src/components/modals/AddItemModal.jsx`

Modal for adding new fridge items.

**Props:**
- `onClose` - Close modal callback
- `onAdd` - Submit item callback

**Fields:** name (required), qty, expiration

---

### EditItemModal

**Location:** `src/components/modals/EditItemModal.jsx`

Modal for editing existing fridge items.

**Props:**
- `item` - Item being edited
- `onClose` - Close modal callback
- `onSave` - Save changes callback

Pre-fills form with current item data.

---

### AddShoppingItemModal

**Location:** `src/components/modals/AddShoppingItemModal.jsx`

Modal for adding shopping list items.

**Props:**
- `onClose` - Close modal callback
- `onAdd` - Submit item callback

**Fields:** name (required), qty

---

## Common Patterns

### Component Communication

State lives in App.jsx and flows down via props. Child components call callbacks to request changes.

```javascript
// Parent
<ChildComponent items={items} onUpdate={handleUpdate} />

// Child
props.onUpdate(newData);
```

### Modal Usage

```javascript
{isOpen && (
  <Modal
    onClose={() => setIsOpen(false)}
    onSubmit={handleSubmit}
  />
)}
```

## Styling

All components use classes from `src/assets/styles/App.css`

**Common Classes:**
- `.panel` - Content container
- `.btn` - Button styles
- `.btn.primary` - Primary button
- `.modal` - Modal container
- `.form-input` - Input fields