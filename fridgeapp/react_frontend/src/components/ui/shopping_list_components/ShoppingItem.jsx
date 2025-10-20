import React from 'react';

export default function ShoppingItem({ item, onRemove, onMarkBought }) {
  const { name, qty, expiration } = item || {};
  const expired = expiration ? (new Date(expiration) < new Date()) : false;

  return (
    <div className="shopping-item" style={{
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: 10, marginBottom: 8, borderRadius: 6, background: 'rgba(255,255,255,0.02)'
    }}>
      <div>
        <div style={{ fontWeight: 600 }}>{name}</div>
        <div style={{ fontSize: 12, color: '#9fb7b9' }}>
          Qty: {qty} {expiration ? `• Exp: ${expiration}` : '• No expiration'}
          {expired && <span style={{ marginLeft: 8, color: '#f88' }}>Expired</span>}
        </div>
      </div>

      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={onMarkBought}>Mark bought</button>
        <button onClick={onRemove}>Remove</button>
      </div>
    </div>
  );
}