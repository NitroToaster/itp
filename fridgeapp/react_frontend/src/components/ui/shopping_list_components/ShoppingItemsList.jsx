import React from 'react';
import ShoppingItem from './ShoppingItem';

export default function ShoppingItemsList({ items = [], selectedId, onSelect, onRemove, onMarkBought }) {
  if (!items.length) return <div className="empty">No shopping items</div>;

  return (
    <div style={{ maxHeight: '60vh', overflow: 'auto' }}>
      {items.map(it => (
        <div
          key={it.id ?? `${it.name}-${it.qty}`}
          onClick={() => onSelect && onSelect(it.id)}
          style={{
            cursor: 'pointer',
            outline: selectedId === it.id ? `2px solid rgba(74,144,217,0.22)` : 'none',
            borderRadius: 8,
            marginBottom: 8,
          }}
        >
          <ShoppingItem item={it} onRemove={() => onRemove && onRemove(it.id)} onMarkBought={() => onMarkBought && onMarkBought(it.id)} />
        </div>
      ))}
    </div>
  );
}