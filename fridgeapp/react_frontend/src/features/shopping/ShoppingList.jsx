import React, { useMemo, useState } from 'react';
import AddShoppingItemModal from '../../components/modals/AddShoppingItemModal';

export default function ShoppingList({
  items = [],
  loading = false,
  onRemove,
  onAdd,
  onMarkBought,
  reload,
}) {
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [busy, setBusy] = useState(false);

  const filteredItems = useMemo(() => {
    const q = (search || '').trim().toLowerCase();
    if (!q) return items || [];
    return (items || []).filter(it => {
      const name = (it.name || '').toLowerCase();
      return name.startsWith(q);
    });
  }, [items, search]);

  const handleAdd = async (item) => {
    if (!onAdd) return;
    setBusy(true);
    try {
      await onAdd(item);
      if (reload) await reload();
    } finally {
      setBusy(false);
    }
  };

  const handleRemoveSelected = async () => {
    if (selectedIds.size === 0 || !onRemove) return;
    setBusy(true);
    try {
      await Promise.all([...selectedIds].map(id => onRemove(id)));
      setSelectedIds(new Set());
      if (reload) await reload();
    } finally {
      setBusy(false);
    }
  };

  const handleAddAllToInventory = async () => {
    if (!items.length || !onMarkBought) return;
    setBusy(true);
    try {
      await Promise.all(items.map(it => onMarkBought(it.id)));
      setSelectedIds(new Set());
      if (reload) await reload();
    } finally {
      setBusy(false);
    }
  };

  const handleRemoveAll = async () => {
    if (!items.length || !onRemove) return;
    setBusy(true);
    try {
      await Promise.all(items.map(it => onRemove(it.id)));
      setSelectedIds(new Set());
      if (reload) await reload();
    } finally {
      setBusy(false);
    }
  };

  const toggleSelection = (id) => {
    setSelectedIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  return (
    <div className="panel">
      {/* Search Section */}
      <div className="section">
        <div style={{ marginBottom: '16px' }}>
          <label className="form-label">Search for item</label>
          <input
            type="text"
            className="form-input"
            placeholder="Search shopping list..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="controls-row">
          <div className="controls-left">
            <button
              className="btn"
              onClick={() => setShowModal(true)}
              disabled={busy}
              style={{ borderColor: 'var(--accent)', color: 'var(--accent)' }}
            >
              Add Item
            </button>
            <button
              className="btn"
              onClick={handleRemoveSelected}
              disabled={busy || selectedIds.size === 0}
            >
              Remove Item
            </button>
          </div>
          <div className="controls-right">
            <button
              className="btn"
              onClick={handleAddAllToInventory}
              disabled={busy || items.length === 0}
              style={{ borderColor: 'var(--accent)', color: 'var(--accent)' }}
            >
              Add All to Inventory
            </button>
            <button
              className="btn"
              onClick={handleRemoveAll}
              disabled={busy || items.length === 0}
            >
              Remove All
            </button>
          </div>
        </div>
      </div>

      {/* Shopping Items List */}
      <div className="section">
        {loading ? (
          <div className="empty">Loading...</div>
        ) : filteredItems.length === 0 ? (
          <div className="empty">Your shopping list is empty. Add items above to get started.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {filteredItems.map((item) => (
              <div
                key={item.id}
                className={`shopping-item ${selectedIds.has(item.id) ? 'selected' : ''}`}
                style={{
                  cursor: 'pointer',
                  borderColor: selectedIds.has(item.id) ? 'var(--accent)' : 'var(--border)',
                }}
                onClick={() => toggleSelection(item.id)}
              >
                <input
                  type="checkbox"
                  checked={selectedIds.has(item.id)}
                  onChange={() => toggleSelection(item.id)}
                  onClick={(e) => e.stopPropagation()}
                />
                <div className="content">
                  <div className="name">{item.name}</div>
                  <div className="meta">Quantity: {item.qty ?? 1}</div>
                </div>
                <button
                  className="btn danger"
                  onClick={(e) => {
                    e.stopPropagation();
                    onRemove && onRemove(item.id);
                  }}
                  style={{ opacity: selectedIds.has(item.id) ? 1 : 0 }}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Shopping Item Modal */}
      {showModal && (
        <AddShoppingItemModal
          onClose={() => setShowModal(false)}
          onAdd={async (item) => {
            await handleAdd(item);
            setShowModal(false);
          }}
        />
      )}
    </div>
  );
}