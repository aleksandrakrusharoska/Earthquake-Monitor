import React from 'react';

function FilterPanel({ filters, onChange, onClear }) {
  const handleChange = (e) => {
    const { name, value } = e.target;
    // Clear the other filter when one is set
    if (name === 'minMag') {
      onChange({ minMag: value, afterTime: '' });
    } else {
      onChange({ minMag: '', afterTime: value });
    }
  };

  const hasFilter = filters.minMag || filters.afterTime;

  return (
    <div>
      <h3>Filters</h3>
      <div className="filter-group">
        <label>Min Magnitude</label>
        <input
          type="number"
          name="minMag"
          placeholder="e.g. 3.0"
          value={filters.minMag}
          onChange={handleChange}
          min="0"
          max="10"
          step="0.1"
        />
      </div>
      <div className="filter-group">
        <label>After Time</label>
        <input
          type="datetime-local"
          name="afterTime"
          value={filters.afterTime}
          onChange={handleChange}
        />
      </div>
      {hasFilter && (
        <button className="btn-clear" onClick={onClear}>
          Clear Filters
        </button>
      )}
    </div>
  );
}

export default FilterPanel;
