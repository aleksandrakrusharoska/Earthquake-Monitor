import React from 'react';

function StatsBar({ earthquakes }) {
  const total = earthquakes.length;
  const maxMag = total > 0 ? Math.max(...earthquakes.map(e => e.magnitude || 0)).toFixed(1) : '—';
  const avgMag = total > 0
    ? (earthquakes.reduce((s, e) => s + (e.magnitude || 0), 0) / total).toFixed(2)
    : '—';
  const significant = earthquakes.filter(e => e.magnitude >= 5.0).length;

  return (
    <div className="stats-bar">
      <div className="stat-card">
        <div className="stat-label">Total Events</div>
        <div className="stat-value accent">{total}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Max Magnitude</div>
        <div className="stat-value danger">{maxMag}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">Avg Magnitude</div>
        <div className="stat-value">{avgMag}</div>
      </div>
      <div className="stat-card">
        <div className="stat-label">M5.0+ Events</div>
        <div className="stat-value warn">{significant}</div>
      </div>
    </div>
  );
}

export default StatsBar;
