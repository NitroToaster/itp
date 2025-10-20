import React from 'react';

export default function FridgeSearch({
  searchText, setSearchText,
  matchMode, setMatchMode,
  sortBy, setSortBy,
  onSearch, onClear,
}) {
  return (
    <div className="section" style={{ marginBottom: 8 }}>
      <h4 className="section-header">Search &amp; Sort</h4>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 160px 160px 90px 90px', gap: 8, alignItems: 'center' }}>
        <input
          className="search-input"
          placeholder="Search by name..."
          value={searchText}
          onChange={e => setSearchText(e.target.value)}
        />

        <select value={matchMode} onChange={e => setMatchMode(e.target.value)} className="input">
          <option value="contains">CONTAINS</option>
          <option value="starts">STARTS WITH</option>
          <option value="exact">EXACT</option>
        </select>

        <select value={sortBy} onChange={e => setSortBy(e.target.value)} className="input">
          <option value="default">DEFAULT</option>
          <option value="name">NAME</option>
          <option value="expiration">EXPIRATION</option>
        </select>

        <button className="btn primary" onClick={onSearch}>Search</button>
        <button className="btn" onClick={onClear}>Clear</button>
      </div>
    </div>
  );
}