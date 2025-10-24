import React, { useRef } from 'react';
import { loadDataFromFile } from '../../services/DataService';

export default function Sidebar({ collapsed, onToggle, onNavigate, onDataLoaded }) {
  const fileInputRef = useRef(null);

  const handleFileClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const result = await loadDataFromFile(file);
      
      if (result.success) {
        alert(result.message);
        // Trigger reload of data in parent component
        if (onDataLoaded) {
          onDataLoaded();
        }
      }
      
      // Reset input so same file can be selected again
      e.target.value = '';
    } catch (error) {
      console.error('Error loading file:', error);
      alert(error.message || 'Error loading file. Please check the console for details.');
    }
  };

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        <div className="sidebar-header-top">
          <div>
            <div className="sidebar-title">FridgeApp</div>
            <div className="sidebar-subtitle">Kitchen command center</div>
          </div>
          <button className="sidebar-toggle-btn" onClick={onToggle}>
            {collapsed ? '☰' : '‹'}
          </button>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="sidebar-group">
          <div className="sidebar-heading">Overview</div>
          <button className="sidebar-button" onClick={() => onNavigate && onNavigate('fridge')}>
            <div className="sidebar-button-icon">🧊</div>
            <span className="sidebar-button-text">Fridge Overview</span>
          </button>
          <button className="sidebar-button" onClick={() => onNavigate && onNavigate('shopping')}>
            <div className="sidebar-button-icon">🛒</div>
            <span className="sidebar-button-text">Shopping List</span>
          </button>
        </div>

        <div className="sidebar-group">
          <div className="sidebar-heading">Data</div>
          <button className="sidebar-button" onClick={handleFileClick}>
            <div className="sidebar-button-icon">📊</div>
            <span className="sidebar-button-text">Load Data</span>
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept=".json"
            onChange={handleFileChange}
            style={{ display: 'none' }}
          />
        </div>
      </nav>

      <div className="sidebar-footer">Groceries made simple</div>
    </aside>
  );
}