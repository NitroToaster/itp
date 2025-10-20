import React, { useState } from 'react';

export default function AddShoppingItemModal({ onClose, onAdd }) {
  const [name, setName] = useState('');
  const [qty, setQty] = useState(1);

  const submit = e => {
    e.preventDefault();
    if (!name.trim()) return;
    onAdd({ name: name.trim(), qty: Number(qty), expiration: null });
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()} style={{ width: 360 }}>
        <h3>Add shopping item</h3>
        <form onSubmit={submit}>
          <label>
            Name:
            <input value={name} onChange={e => setName(e.target.value)} required autoFocus />
          </label>

          <label>
            Quantity:
            <input type="number" min="1" value={qty} onChange={e => setQty(e.target.value)} />
          </label>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
            <button type="submit">Add</button>
            <button type="button" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}