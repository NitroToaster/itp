import React, { useMemo, useState } from 'react';
import ShoppingSearch from '../components/ui/shopping_list_components/ShoppingSearch';
import ShoppingControls from '../components/ui/shopping_list_components/ShoppingControls';
import ShoppingItemsList from '../components/ui/shopping_list_components/ShoppingItemsList';
import AddShoppingItemModal from '../components/ui/shopping_list_components/AddShoppingItemModal';

export default function ShoppingList({
  items = [],
  loading = false,
  onRemove,
  onAdd, // optional: (item) => void
  onMarkBought, // optional: (id) => void
  onBack,
  onAddAllToInventory,
  onRemoveAll,
  reload, // optional
}) {
  // page-owned UI state (same logic you had)
  const [search, setSearch] = useState('');
  const [matchMode, setMatchMode] = useState('contains'); // contains | starts | exact
  const [qtyThreshold, setQtyThreshold] = useState(1);
  const [includeNoExpiration, setIncludeNoExpiration] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [busy, setBusy] = useState(false);

  const isExpired = exp => {
    if (!exp) return false;
    try { return new Date(exp) < new Date(); } catch { return false; }
  };

  const toBuy = useMemo(() => {
    const q = (search || '').trim().toLowerCase();
    return (items || []).filter(it => {
      if (!includeNoExpiration && !it.expiration) return false;
      if ((it.qty ?? 0) > qtyThreshold) return false; // keep items with qty <= threshold
      if (isExpired(it.expiration)) return true;
      if (!q) return true;
      const name = (it.name || '').toLowerCase();
      if (matchMode === 'contains') return name.includes(q);
      if (matchMode === 'starts') return name.startsWith(q);
      if (matchMode === 'exact') return name === q;
      return true;
    });
  }, [items, search, matchMode, qtyThreshold, includeNoExpiration]);

  const handleAdd = async item => {
    if (!onAdd) return;
    setBusy(true);
    try {
      await onAdd(item);
      if (reload) await reload();
    } finally { setBusy(false); }
  };

  const handleRemoveSelected = async () => {
    if (!selectedId || !onRemove) return;
    setBusy(true);
    try {
      await onRemove(selectedId);
      setSelectedId(null);
      if (reload) await reload();
    } finally { setBusy(false); }
  };

  const handleRemoveAll = async () => {
    if (!onRemoveAll && !onRemove) return;
    setBusy(true);
    try {
      if (onRemoveAll) {
        await onRemoveAll();
      } else {
        await Promise.all((items || []).map(it => onRemove(it.id)));
      }
      setSelectedId(null);
      if (reload) await reload();
    } finally { setBusy(false); }
  };

  const handleAddAllToInventory = async () => {
    if (!onAddAllToInventory && !onMarkBought) return;
    setBusy(true);
    try {
      if (onAddAllToInventory) {
        await onAddAllToInventory();
      } else {
        await Promise.all((items || []).map(it => onMarkBought && onMarkBought(it.id)));
      }
      setSelectedId(null);
      if (reload) await reload();
    } finally { setBusy(false); }
  };

  return (
    <div className="panel shopping-root">
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Shopping List</h2>
      </header>

      <ShoppingSearch
        search={search}
        setSearch={setSearch}
        matchMode={matchMode}
        setMatchMode={setMatchMode}
        qtyThreshold={qtyThreshold}
        setQtyThreshold={setQtyThreshold}
        includeNoExpiration={includeNoExpiration}
        setIncludeNoExpiration={setIncludeNoExpiration}
      />

      <ShoppingControls
        onAddClick={() => setShowModal(true)}
        onRemoveSelected={handleRemoveSelected}
        onBack={() => onBack && onBack()}
        onAddAllToInventory={handleAddAllToInventory}
        onRemoveAll={handleRemoveAll}
        onRefresh={reload}
        disableActions={busy || loading}
        selectedId={selectedId}
      />

      {loading ? <div className="empty">Loading...</div> : (
        <ShoppingItemsList
          items={toBuy}
          selectedId={selectedId}
          onSelect={id => setSelectedId(id)}
          onRemove={onRemove}
          onMarkBought={onMarkBought}
        />
      )}

      {showModal && (
        <AddShoppingItemModal
          onClose={() => setShowModal(false)}
          onAdd={async item => {
            await handleAdd(item);
            setShowModal(false);
          }}
        />
      )}
    </div>
  );
}
