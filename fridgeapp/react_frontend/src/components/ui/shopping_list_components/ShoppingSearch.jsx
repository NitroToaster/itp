import React from 'react';

export default function ShoppingSearch({
  search,
  setSearch,
  matchMode,
  setMatchMode,
  qtyThreshold,
  setQtyThreshold,
  includeNoExpiration,
  setIncludeNoExpiration,
}) {
  return (
    <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 }}>
      <input
        className="input"
        placeholder="Search item..."
        value={search}
        onChange={e => setSearch(e.target.value)}
        style={{ flex: 1 }}
      />

      <select className="input" value={matchMode} onChange={e => setMatchMode(e.target.value)}>
        <option value="contains">CONTAINS</option>
        <option value="starts">STARTS WITH</option>
        <option value="exact">EXACT</option>
      </select>

      <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
        Qty ≤
        <input
          className="input"
          type="number"
          min="0"
          value={qtyThreshold}
          onChange={e => setQtyThreshold(Number(e.target.value))}
          style={{ width: 72 }}
        />
      </label>

      <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
        <input type="checkbox" checked={includeNoExpiration} onChange={e => setIncludeNoExpiration(e.target.checked)} />
        Include no-expiration
      </label>
    </div>
  );
}