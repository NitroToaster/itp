import React, { useState } from 'react';

export default function AddItemModal({ onClose, onAdd }) {
  const [name, setName] = useState('');
  const [qty, setQty] = useState(1);
  const [expiration, setExpiration] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    onAdd({ name, qty: Number(qty), expiration });
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">Add Item to Fridge</h3>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label className="form-label">Item Name</label>
              <input
                type="text"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g., Milk"
                autoFocus
              />
            </div>
            <div className="form-group">
              <label className="form-label">Quantity</label>
              <input
                type="number"
                className="form-input"
                value={qty}
                onChange={(e) => setQty(e.target.value)}
                min="1"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Expiration Date</label>
              <input
                type="date"
                className="form-input"
                value={expiration}
                onChange={(e) => setExpiration(e.target.value)}
              />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn primary">
              Add Item
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}