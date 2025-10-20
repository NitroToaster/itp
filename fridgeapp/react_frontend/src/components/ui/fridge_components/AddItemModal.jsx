import React, { useState } from 'react';

export default function AddItemModal({ onClose, onAdd }) {
  const [name, setName] = useState('');
  const [qty, setQty] = useState(1);
  const [expiration, setExpiration] = useState('');

  const submit = e => {
    e.preventDefault();
    if (!name.trim()) return;
    onAdd({ name: name.trim(), qty: Number(qty), expiration: expiration || null });
  };

  return (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal" onClick={e => e.stopPropagation()}>
        <h3>Add item to fridge</h3>
        <form onSubmit={submit}>
          <label>
            Name:
            <input
              type="text"
              required
              value={name}
              onChange={e => setName(e.target.value)}
              autoFocus
            />
          </label>

          <label>
            Quantity:
            <input
              type="number"
              min="1"
              value={qty}
              onChange={e => setQty(e.target.value)}
            />
          </label>

          <label>
            Expiration:
            <input
              type="date"
              value={expiration}
              onChange={e => setExpiration(e.target.value)}
            />
          </label>

          <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
            <button type="submit" className="btn primary">Add</button>
            <button type="button" className="btn" onClick={onClose}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}