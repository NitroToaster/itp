import React, { useState } from 'react';
import Sidebar from './Sidebar';

export default function AppShell({ children, onNavigate, onDataLoaded }) {
  const [collapsed, setCollapsed] = useState(false);
  return (
    <div className={`App ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <Sidebar 
        collapsed={collapsed} 
        onToggle={() => setCollapsed(c => !c)} 
        onNavigate={onNavigate}
        onDataLoaded={onDataLoaded}
      />
      <main className="workspace panel">
        {children}
      </main>
    </div>
  );
}