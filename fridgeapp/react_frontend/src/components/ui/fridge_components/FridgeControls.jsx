import React from 'react';

export default function FridgeControls({ onOpenAdd, onOpenShopping, reload }) {
  return (
    <div className="section" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <div>
        <button className="btn primary" onClick={onOpenAdd}>Add Item</button>
      </div>

      <div style={{ flex: 1 }} />

      <div style={{ display: 'flex', gap: 8 }}>
        <button className="btn ghost" onClick={onOpenShopping}>Shopping List</button>
        <button className="btn" onClick={reload}>Refresh</button>
      </div>
    </div>
  );
}