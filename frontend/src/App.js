import React, { useState, useEffect, useCallback } from 'react';
import earthquakeService from './services/earthquakeService';
import EarthquakeTable from './components/EarthquakeTable';
import EarthquakeMap from './components/EarthquakeMap';
import FilterPanel from './components/FilterPanel';
import StatsBar from './components/StatsBar';
import './App.css';

function App() {
  const [earthquakes, setEarthquakes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);
  const [activeView, setActiveView] = useState('table');
  const [filters, setFilters] = useState({ minMag: '', afterTime: '' });

  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(null), 4000);
  };

  const loadEarthquakes = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      let res;
      if (filters.minMag && filters.minMag !== '') {
        res = await earthquakeService.filterByMagnitude(parseFloat(filters.minMag));
      } else if (filters.afterTime && filters.afterTime !== '') {
        const ms = new Date(filters.afterTime).getTime();
        res = await earthquakeService.filterByTime(ms);
      } else {
        res = await earthquakeService.getAll();
      }
      setEarthquakes(res.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load earthquake data.');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    loadEarthquakes();
  }, [loadEarthquakes]);

  const handleFetch = async () => {
    setFetching(true);
    setError(null);
    try {
      const res = await earthquakeService.fetchAndStore();
      showSuccess(`Fetched ${res.data.data?.length || 0} earthquakes from USGS API`);
      await loadEarthquakes();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch from USGS API. Is the backend running?');
    } finally {
      setFetching(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await earthquakeService.deleteById(id);
      showSuccess('Earthquake record deleted.');
      setEarthquakes(prev => prev.filter(e => e.id !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete.');
    }
  };

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
  };

  const clearFilters = () => {
    setFilters({ minMag: '', afterTime: '' });
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-inner">
          <div className="header-brand">
            <div className="brand-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="3" />
                <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83" />
              </svg>
            </div>
            <div>
              <h1 className="brand-title">EarthquakeMonitor</h1>
              <p className="brand-sub">USGS Real-time Data</p>
            </div>
          </div>
          <button
            className={`btn-fetch ${fetching ? 'loading' : ''}`}
            onClick={handleFetch}
            disabled={fetching}
          >
            {fetching ? (
              <><span className="spinner" />Fetching...</>
            ) : (
              <><span className="icon-refresh">↻</span> Fetch Latest</>
            )}
          </button>
        </div>
      </header>

      <main className="app-main">
        {error && (
          <div className="alert alert-error">
            <span>⚠</span> {error}
            <button onClick={() => setError(null)}>✕</button>
          </div>
        )}
        {successMsg && (
          <div className="alert alert-success">
            <span>✓</span> {successMsg}
          </div>
        )}

        <StatsBar earthquakes={earthquakes} />

        <div className="content-layout">
          <aside className="sidebar">
            <FilterPanel
              filters={filters}
              onChange={handleFilterChange}
              onClear={clearFilters}
            />
          </aside>

          <section className="content-area">
            <div className="view-controls">
              <button
                className={`view-btn ${activeView === 'table' ? 'active' : ''}`}
                onClick={() => setActiveView('table')}
              >
                Table View
              </button>
              <button
                className={`view-btn ${activeView === 'map' ? 'active' : ''}`}
                onClick={() => setActiveView('map')}
              >
                Map View
              </button>
            </div>

            {loading ? (
              <div className="loading-state">
                <div className="loading-ring" />
                <p>Loading earthquake data...</p>
              </div>
            ) : earthquakes.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">◎</div>
                <h3>No earthquake data</h3>
                <p>Click "Fetch Latest" to load earthquake data from the USGS API.</p>
              </div>
            ) : activeView === 'table' ? (
              <EarthquakeTable
                earthquakes={earthquakes}
                onDelete={handleDelete}
              />
            ) : (
              <EarthquakeMap earthquakes={earthquakes} />
            )}
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;
