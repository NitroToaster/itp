import React, { useMemo, useState } from 'react';
import FridgeSearch from '../components/ui/fridge_components/FridgeSearch';
import FridgeFilters from '../components/ui/fridge_components/FridgeFilters';
import FridgeControls from '../components/ui/fridge_components/FridgeControls';
import ItemList from '../components/ui/fridge_components/ItemList';
import AddItemModal from '../components/ui/fridge_components/AddItemModal';

export default function Fridge({
  items = [],
  loading,
  onAdd,
  onRemove,
  reload,
  onOpenShopping, // optional: function to switch to shopping view
}) {
  // search state
  const [searchText, setSearchText] = useState('');
  const [matchMode, setMatchMode] = useState('contains'); // contains | starts | exact
  const [sortBy, setSortBy] = useState('default'); // default | name | expiration

  // filter builder (live inputs)
  const [qtyMin, setQtyMin] = useState('');
  const [qtyMax, setQtyMax] = useState('');
  const [expFrom, setExpFrom] = useState('');
  const [expUntil, setExpUntil] = useState('');
  const [includeUnknown, setIncludeUnknown] = useState(true);

  // applied filters (set when user clicks Apply Filter)
  const [appliedFilters, setAppliedFilters] = useState(null);

  // modal
  const [modalOpen, setModalOpen] = useState(false);

  const applyFilter = () => {
    setAppliedFilters({
      qtyMin: qtyMin === '' ? null : Number(qtyMin),
      qtyMax: qtyMax === '' ? null : Number(qtyMax),
      expFrom: expFrom || null,
      expUntil: expUntil || null,
      includeUnknown,
    });
  };

  const clearFilters = () => {
    setQtyMin(''); setQtyMax(''); setExpFrom(''); setExpUntil(''); setIncludeUnknown(true);
    setAppliedFilters(null);
  };

  const clearSearch = () => {
    setSearchText(''); setMatchMode('contains');
  };

  const filterAndSort = useMemo(() => {
    const s = (searchText || '').trim().toLowerCase();
    const af = appliedFilters;
    const list = (items || []).filter(it => {
      // expiration include / unknown
      if (af) {
        if (!af.includeUnknown && !it.expiration) return false;
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

      // search match
      if (!s) return true;
      const name = (it.name || '').toLowerCase();
      if (matchMode === 'contains') return name.includes(s);
      if (matchMode === 'starts') return name.startsWith(s);
      if (matchMode === 'exact') return name === s;
      return true;
    });

    // sort
    const out = list.slice();
    if (sortBy === 'name') {
      out.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    } else if (sortBy === 'expiration') {
      out.sort((a, b) => {
        const da = a.expiration ? new Date(a.expiration) : null;
        const db = b.expiration ? new Date(b.expiration) : null;
        if (!da && !db) return 0;
        if (!da) return 1;
        if (!db) return -1;
        return da - db;
      });
    }
    return out;
  }, [items, appliedFilters, searchText, matchMode, sortBy]);

  return (
    <div className="page-fridge panel">
      <FridgeSearch
        searchText={searchText}
        setSearchText={setSearchText}
        matchMode={matchMode}
        setMatchMode={setMatchMode}
        sortBy={sortBy}
        setSortBy={setSortBy}
        onSearch={() => {/* search is applied live in filterAndSort; keep for parity */}}
        onClear={clearSearch}
      />

      <FridgeFilters
        qtyMin={qtyMin}
        setQtyMin={setQtyMin}
        qtyMax={qtyMax}
        setQtyMax={setQtyMax}
        expFrom={expFrom}
        setExpFrom={setExpFrom}
        expUntil={expUntil}
        setExpUntil={setExpUntil}
        includeUnknown={includeUnknown}
        setIncludeUnknown={setIncludeUnknown}
        onApply={applyFilter}
        onClear={clearFilters}
      />

      <FridgeControls
        onOpenAdd={() => setModalOpen(true)}
        onOpenShopping={() => onOpenShopping && onOpenShopping()}
        reload={reload}
      />

      <div style={{ marginTop: 6 }}>
        <h3 className="section-header">Fridge Items</h3>
        {loading ? <div>Loading...</div> : <ItemList items={filterAndSort} onRemove={onRemove} />}
      </div>

      {modalOpen && (
        <AddItemModal
          onClose={() => setModalOpen(false)}
          onAdd={async item => {
            await onAdd(item);
            setModalOpen(false);
          }}
        />
      )}
    </div>
  );
}