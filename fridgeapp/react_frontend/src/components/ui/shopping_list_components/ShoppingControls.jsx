import React from 'react';

export default function ShoppingControls({
  onAddClick,
  onRemoveSelected,
  onBack,
  onAddAllToInventory,
  onRemoveAll,
  onRefresh,
  disableActions = false,
  selectedId,
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn primary" onClick={onAddClick} disabled={disableActions}>Add Item</button>
        <button className="btn primary" onClick={onRemoveSelected} disabled={disableActions || !selectedId}>Remove Item</button>
      </div>

      <div style={{ flex: 1 }} />

      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn primary" onClick={onBack} disabled={disableActions}>Back to Fridge</button>
        <button className="btn primary" onClick={onAddAllToInventory} disabled={disableActions}>Add All to Inventory</button>
        <button className="btn primary" onClick={onRemoveAll} disabled={disableActions}>Remove All</button>
        {onRefresh && <button className="btn" onClick={onRefresh}>Refresh</button>}
      </div>
    </div>
  );
}