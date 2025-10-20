import React from 'react';

function fmtDate(d) {
  if (!d) return '—';
  try {
    const dt = new Date(d);
    return dt.toLocaleDateString();
  } catch {
    return d;
  }
}

export default function ItemList({ items = [], onRemove }) {
  if (!items.length) {
    return <div className="empty">No items</div>;
  }

  return (
    <div className="item-list">
      {items.map(it => (
        <div className="item-row" key={it.id ?? `${it.name}-${it.expiration}`}>
          <div className="item-left">
            <div className="item-name">{it.name}</div>
            <div className="item-meta">
              Qty: {it.qty ?? 0} • Exp: {fmtDate(it.expiration)}
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn" onClick={() => onRemove && onRemove(it.id)}>Remove</button>
          </div>
        </div>
      ))}
    </div>
  );
}