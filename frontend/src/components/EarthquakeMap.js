import React, {useEffect, useRef} from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

function getMagColor(mag) {
    if (mag < 3.0) return '#22c55e';
    if (mag < 5.0) return '#f59e0b';
    if (mag < 7.0) return '#ef4444';
    return '#7c3aed';
}

function EarthquakeMap({earthquakes}) {
    const mapRef = useRef(null);
    const mapInstanceRef = useRef(null);
    const markersRef = useRef([]);

    useEffect(() => {
        if (mapInstanceRef.current) return;

        const container = mapRef.current;
        if (!container) return;

        if (container._leaflet_id) {
            container._leaflet_id = null;
        }

        mapInstanceRef.current = L.map(mapRef.current, {
            center: [20, 0],
            zoom: 2,
            minZoom: 2,
            maxZoom: 18,
            maxBounds: [[-90, -180], [90, 180]],
            maxBoundsViscosity: 1.0
        });

        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
            attribution: '© OpenStreetMap, © CARTO',
            maxZoom: 19
        }).addTo(mapInstanceRef.current);

        return () => {
            if (mapInstanceRef.current) {
                mapInstanceRef.current.remove();
                mapInstanceRef.current = null;
            }
        };
    }, []);

    useEffect(() => {
        const map = mapInstanceRef.current;
        if (!map) return;

        markersRef.current.forEach(m => m.remove());
        markersRef.current = [];

        const valid = earthquakes.filter(
            eq => eq.latitude != null && eq.longitude != null
        );

        valid.forEach(eq => {
            const radius = Math.max(4, (eq.magnitude || 0) * 2.5);
            const color = getMagColor(eq.magnitude || 0);
            const circle = L.circleMarker([eq.latitude, eq.longitude], {
                radius,
                color,
                fillColor: color,
                fillOpacity: 0.7,
                weight: 1,
                opacity: 0.9
            });

            circle.bindPopup(`
        <div style="font-family: monospace; font-size: 12px; color: #e8edf3; background: #131920; padding: 8px; border-radius: 6px; min-width: 200px;">
          <div style="font-weight: 700; color: ${color}; font-size: 14px; margin-bottom: 4px;">M ${eq.magnitude?.toFixed(1)}</div>
          <div style="color: #8a9ab0; margin-bottom: 2px;">${eq.place}</div>
          <div style="color: #4d5f74; font-size: 11px;">${eq.latitude?.toFixed(3)}, ${eq.longitude?.toFixed(3)}</div>
          ${eq.depth != null ? `<div style="color: #4d5f74; font-size: 11px;">Depth: ${eq.depth.toFixed(1)} km</div>` : ''}
        </div>
      `, {
                className: 'dark-popup'
            });

            circle.addTo(map);
            markersRef.current.push(circle);
        });

        if (valid.length > 0) {
            const bounds = L.latLngBounds(valid.map(eq => [eq.latitude, eq.longitude]));
            map.fitBounds(bounds, {padding: [40, 40]});
        }
    }, [earthquakes]);

    return (
        <div className="map-container">
            <div ref={mapRef} style={{height: '100%', width: '100%'}}/>
        </div>
    );
}

export default EarthquakeMap;
