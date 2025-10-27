import React, { useMemo, useState } from 'react';
import AddItemModal from '../../components/modals/AddItemModal';
import EditItemModal from '../../components/modals/EditItemModal';

export default function Fridge({
  items = [],
  loading,
  onAdd,
  onRemove,
  onUpdate,
  reload,
  onOpenShopping,
}) {
  const [searchText, setSearchText] = useState('');
  const [sortBy, setSortBy] = useState('default');
  const [modalOpen, setModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [showFilters, setShowFilters] = useState(false);

  // Filter states
  const [qtyMin, setQtyMin] = useState('');
  const [qtyMax, setQtyMax] = useState('');
  const [expFrom, setExpFrom] = useState('');
  const [expUntil, setExpUntil] = useState('');
  const [appliedFilters, setAppliedFilters] = useState(null);

  const applyFilter = () => {
    setAppliedFilters({
      qtyMin: qtyMin === '' ? null : Number(qtyMin),
      qtyMax: qtyMax === '' ? null : Number(qtyMax),
      expFrom: expFrom || null,
      expUntil: expUntil || null,
    });
  };

  const clearFilters = () => {
    setQtyMin('');
    setQtyMax('');
    setExpFrom('');
    setExpUntil('');
    setAppliedFilters(null);
  };

  const handleEdit = (item) => {
    setEditingItem(item);
    setEditModalOpen(true);
  };

  const handleUpdate = async (updatedItem) => {
    if (onUpdate) {
      await onUpdate(updatedItem);
    }
    setEditModalOpen(false);
    setEditingItem(null);
    if (reload) await reload();
  };

  const filteredItems = useMemo(() => {
    const s = (searchText || '').trim().toLowerCase();
    const af = appliedFilters;

    let list = (items || []).filter(it => {
      if (af) {
        if (af.qtyMin != null && (it.qty ?? 0) < af.qtyMin) return false;
        if (af.qtyMax != null && (it.qty ?? 0) > af.qtyMax) return false;
        if (af.expFrom) {
          const d = it.expiration ? new Date(it.expiration) : null;
          if (!d || d < new Date(af.expFrom)) return false;
        }
        if (af.expUntil) {
          const d = it.expiration ? new Date(it.expiration) : null;
          if (!d || d > new Date(af.expUntil + 'T23:59:59')) return false;
        }
      }

      if (!s) return true;
      const name = (it.name || '').toLowerCase();
      return name.includes(s);
    });

    if (sortBy === 'name') {
      list.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    } else if (sortBy === 'expiration') {
      list.sort((a, b) => {
        const da = a.expiration ? new Date(a.expiration) : null;
        const db = b.expiration ? new Date(b.expiration) : null;
        if (!da && !db) return 0;
        if (!da) return 1;
        if (!db) return -1;
        return da - db;
      });
    } else if (sortBy === 'quantity') {
      list.sort((a, b) => ((a.qty ?? 0) - (b.qty ?? 0)));
    }

    return list;
  }, [items, appliedFilters, searchText, sortBy]);

  const formatDate = (d) => {
    if (!d) return 'N/A';
    try {
      return new Date(d).toLocaleDateString();
    } catch {
      return d;
    }
  };

  const getExpirationStatus = (expirationDate) => {
    if (!expirationDate) return { color: '#666666', label: 'No expiration' };
    
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    const expDate = new Date(expirationDate);
    expDate.setHours(0, 0, 0, 0);
    
    const diffTime = expDate - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays < 0) {
      return { color: '#ef4444', label: 'Expired' };
    } else if (diffDays <= 2) {
      return { color: '#fb923c', label: '0-2 days' };
    } else if (diffDays <= 5) {
      return { color: '#fbbf24', label: '2-5 days' };
    } else {
      return { color: '#22c55e', label: '5+ days' };
    }
  };

  return (
    <div className="panel">
      {/* Header */}
      <div className="page-header">
        <h2 className="page-title">Fridge</h2>
        <div className="search-bar">
          <input
            className="search-input"
            placeholder="Search items..."
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
          <button className="btn">Search</button>
          <button className="btn" onClick={() => setSearchText('')}>Clear</button>
        </div>
      </div>

      {/* Filter Controls */}
      <div className="controls-row">
        <div className="controls-left">
          <button className="btn" onClick={() => setShowFilters(!showFilters)}>
            {showFilters ? 'Hide Filters' : 'Filter'}
          </button>
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className="input">
            <option value="default">Default</option>
            <option value="name">Sort by Name</option>
            <option value="expiration">Sort by Expiration</option>
            <option value="quantity">Sort by Quantity</option>
          </select>
        </div>
        <div className="controls-right">
          <button className="btn primary" onClick={() => setModalOpen(true)}>
            Add Item
          </button>
        </div>
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <div className="filter-panel">
          <div className="filter-grid">
            <div className="form-group">
              <label className="form-label">Min Quantity</label>
              <input
                type="number"
                className="form-input"
                value={qtyMin}
                onChange={(e) => setQtyMin(e.target.value)}
                placeholder="Min"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Max Quantity</label>
              <input
                type="number"
                className="form-input"
                value={qtyMax}
                onChange={(e) => setQtyMax(e.target.value)}
                placeholder="Max"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Exp From</label>
              <input
                type="date"
                className="form-input"
                value={expFrom}
                onChange={(e) => setExpFrom(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Exp Until</label>
              <input
                type="date"
                className="form-input"
                value={expUntil}
                onChange={(e) => setExpUntil(e.target.value)}
              />
            </div>
          </div>
          <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
            <button className="btn primary" onClick={applyFilter}>Apply Filters</button>
            <button className="btn" onClick={clearFilters}>Clear Filters</button>
          </div>
        </div>
      )}

      {/* Fridge Items Section */}
      <div className="section">
        <h3 className="section-header">Fridge Items</h3>

        {loading ? (
          <div className="empty">Loading...</div>
        ) : filteredItems.length === 0 ? (
          <div className="empty">No items in fridge. Add some items to get started.</div>
        ) : (
          <div className="table-container">
            <div className="table-header cols-3">
              <div>Name</div>
              <div>Quantity</div>
              <div>Expiration</div>
            </div>
            <div className="table-body">
              {filteredItems.map((item) => {
                const expirationStatus = getExpirationStatus(item.expiration);
                return (
                  <div key={item.id} className="table-row cols-3">
                    <div className="table-cell" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span>{item.name}</span>
                      <span 
                        className="expiration-dot" 
                        style={{ 
                          backgroundColor: expirationStatus.color,
                          width: '8px',
                          height: '8px',
                          borderRadius: '50%',
                          display: 'inline-block',
                          flexShrink: 0
                        }}
                        title={expirationStatus.label}
                      />
                    </div>
                    <div className="table-cell secondary">{item.qty ?? 0}</div>
                    <div className="table-cell secondary" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>{formatDate(item.expiration)}</span>
                      <div className="table-cell-actions">
                        <button
                          className="btn"
                          onClick={() => handleEdit(item)}
                        >
                          Edit
                        </button>
                        <button
                          className="btn danger"
                          onClick={() => onRemove && onRemove(item.id)}
                        >
                          Remove
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>

      {/* Add Item Modal */}
      {modalOpen && (
        <AddItemModal
          onClose={() => setModalOpen(false)}
          onAdd={async (item) => {
            await onAdd(item);
            setModalOpen(false);
          }}
        />
      )}

      {/* Edit Item Modal */}
      {editModalOpen && editingItem && (
        <EditItemModal
          item={editingItem}
          onClose={() => {
            setEditModalOpen(false);
            setEditingItem(null);
          }}
          onSave={handleUpdate}
        />
      )}
    </div>
  );
}