import React from 'react';

export default function FridgeFilters({
  qtyMin, setQtyMin, qtyMax, setQtyMax,
  expFrom, setExpFrom, expUntil, setExpUntil,
  includeUnknown, setIncludeUnknown,
  onApply, onClear,
}) {
  return (
    <div className="section" style={{ marginBottom: 8 }}>
      <h4 className="section-header">Filters</h4>

      <div style={{ display: 'grid', gridTemplateColumns: '60px 95px 60px 95px 1fr 1fr', gap: 8, alignItems: 'center' }}>
        <label>Qty Min:</label>
        <input className="input" type="number" min="0" value={qtyMin} onChange={e => setQtyMin(e.target.value)} />
        <label>Max:</label>
        <input className="input" type="number" min="0" value={qtyMax} onChange={e => setQtyMax(e.target.value)} />
        <input className="input" type="date" value={expFrom} onChange={e => setExpFrom(e.target.value)} placeholder="Expiration From" />
        <input className="input" type="date" value={expUntil} onChange={e => setExpUntil(e.target.value)} placeholder="Expiration Until" />

        <div style={{ gridColumn: '1 / span 4' }}>
          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <input type="checkbox" checked={includeUnknown} onChange={e => setIncludeUnknown(e.target.checked)} />
            Include items without expiration date
          </label>
        </div>

        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button className="btn ghost" onClick={onApply}>Apply Filter</button>
          <button className="btn" onClick={onClear}>Clear Filter</button>
        </div>
      </div>
    </div>
  );
}