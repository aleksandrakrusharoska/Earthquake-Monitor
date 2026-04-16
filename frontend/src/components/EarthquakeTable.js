import React from 'react';
import { format } from 'date-fns';

function getMagClass(mag) {
  if (mag < 3.0) return 'mag-low';
  if (mag < 5.0) return 'mag-med';
  if (mag < 7.0) return 'mag-high';
  return 'mag-extreme';
}

function EarthquakeTable({ earthquakes, onDelete }) {
  return (
    <div className="eq-table-wrap">
      <table className="eq-table">
        <thead>
          <tr>
            <th>Mag</th>
            <th>Type</th>
            <th>Location</th>
            <th>Time (UTC)</th>
            <th>Coordinates</th>
            <th>Depth km</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {earthquakes.map(eq => (
            <tr key={eq.id}>
              <td>
                <span className={`mag-badge ${getMagClass(eq.magnitude)}`}>
                  {eq.magnitude?.toFixed(1)}
                </span>
              </td>
              <td>
                <span className="magtype-text">{eq.magType || '—'}</span>
              </td>
              <td>
                <span className="place-text">{eq.place}</span>
              </td>
              <td>
                <span className="time-text">
                  {eq.time ? format(new Date(eq.time), 'MMM d, HH:mm:ss') : '—'}
                </span>
              </td>
              <td>
                <span className="coords-text">
                  {eq.latitude != null && eq.longitude != null
                    ? `${eq.latitude.toFixed(3)}, ${eq.longitude.toFixed(3)}`
                    : '—'}
                </span>
              </td>
              <td>
                <span className="coords-text">
                  {eq.depth != null ? `${eq.depth.toFixed(1)}` : '—'}
                </span>
              </td>
              <td>
                <button
                  className="btn-delete"
                  onClick={() => {
                    if (window.confirm(`Delete earthquake at ${eq.place}?`)) {
                      onDelete(eq.id);
                    }
                  }}
                >
                  delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default EarthquakeTable;
