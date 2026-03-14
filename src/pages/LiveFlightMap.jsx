import { useEffect, useMemo, useRef, useState } from 'react';
import Globe from 'react-globe.gl';
import { Radar, RefreshCw, Plane, Activity, Route as RouteIcon, Gauge } from 'lucide-react';
import Layout from '../components/Layout';
import { trackingService } from '../services/trackingService';

const EARTH_TEXTURE = 'https://unpkg.com/three-globe/example/img/earth-blue-marble.jpg';
const BUMP_TEXTURE = 'https://unpkg.com/three-globe/example/img/earth-topology.png';
const CLOUDS_TEXTURE = 'https://unpkg.com/three-globe/example/img/earth-clouds.png';

function statusClass(status) {
  if (status === 'DEPARTED') return 'status-landed';
  if (status === 'DELAYED') return 'status-delayed';
  if (status === 'BOARDING') return 'status-boarding';
  return 'status-scheduled';
}

export default function LiveFlightMap() {
  const globeRef = useRef(null);
  const [tracks, setTracks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selected, setSelected] = useState(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await trackingService.getLiveFlights();
      setTracks(data || []);
      if (!selected && data?.length) {
        setSelected(data[0]);
      }
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to fetch live flight tracks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    const id = setInterval(load, 15000);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    if (globeRef.current) {
      globeRef.current.pointOfView({ altitude: 2.2 }, 0);
      globeRef.current.controls().autoRotate = true;
      globeRef.current.controls().autoRotateSpeed = 0.2;
    }
  }, []);

  const arcs = useMemo(() => tracks.map((t) => ({
    startLat: t.departureLat,
    startLng: t.departureLon,
    endLat: t.arrivalLat,
    endLng: t.arrivalLon,
    route: t.route,
    status: t.status,
    color: t.status === 'DELAYED' ? '#f59e0b' : '#3b82f6',
  })), [tracks]);

  const points = useMemo(() => tracks.map((t) => ({
    lat: t.currentLat,
    lng: t.currentLon,
    size: 0.28,
    color: t.status === 'DELAYED' ? '#f59e0b' : '#22c55e',
    track: t,
  })), [tracks]);

  const depArrLabels = useMemo(() => {
    const out = [];
    tracks.forEach((t) => {
      out.push({ lat: t.departureLat, lng: t.departureLon, text: t.departureCode });
      out.push({ lat: t.arrivalLat, lng: t.arrivalLon, text: t.arrivalCode });
    });
    return out;
  }, [tracks]);

  const kpis = {
    live: tracks.length,
    delayed: tracks.filter((t) => t.status === 'DELAYED').length,
    avgSpeed: tracks.length ? Math.round(tracks.reduce((acc, t) => acc + (t.groundSpeedKts || 0), 0) / tracks.length) : 0,
  };

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title"><Radar size={22} /> Live Flight World Map</h1>
          <p className="page-subtitle">Real-time in-air route visualization with moving aircraft and route telemetry</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}>
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}

      <div className="tracking-kpis">
        <div className="tracking-kpi"><Plane size={16} /><strong>{kpis.live}</strong><span>Flights In Air</span></div>
        <div className="tracking-kpi"><Activity size={16} /><strong>{kpis.delayed}</strong><span>Delayed In Air</span></div>
        <div className="tracking-kpi"><Gauge size={16} /><strong>{kpis.avgSpeed}</strong><span>Avg Speed (kts)</span></div>
      </div>

      <div className="tracking-layout">
        <section className="tracking-globe-shell">
          {loading && <div className="tracking-overlay">Loading live tracks…</div>}
          <Globe
            ref={globeRef}
            globeImageUrl={EARTH_TEXTURE}
            bumpImageUrl={BUMP_TEXTURE}
            cloudsImageUrl={CLOUDS_TEXTURE}
            backgroundColor="rgba(0,0,0,0)"
            arcsData={arcs}
            arcColor={(d) => [d.color, d.color]}
            arcAltitude={0.22}
            arcDashLength={0.45}
            arcDashGap={0.5}
            arcDashAnimateTime={1800}
            pointsData={points}
            pointLat="lat"
            pointLng="lng"
            pointAltitude={0.02}
            pointColor="color"
            pointRadius="size"
            labelsData={depArrLabels}
            labelLat="lat"
            labelLng="lng"
            labelText="text"
            labelSize={0.85}
            labelDotRadius={0.24}
            labelColor={() => '#d8e8ff'}
            onPointClick={(p) => setSelected(p.track)}
          />
        </section>

        <aside className="tracking-sidepanel">
          <div className="ops-section">
            <div className="ops-section-header">
              <h2><RouteIcon size={16} /> Active Flights</h2>
              <span className="ops-count">{tracks.length}</span>
            </div>
            <div className="tracking-flight-list">
              {tracks.map((t) => (
                <button
                  key={t.flightId}
                  className={`tracking-flight-item ${selected?.flightId === t.flightId ? 'active' : ''}`}
                  onClick={() => setSelected(t)}
                >
                  <div>
                    <strong>#{t.flightId} {t.route}</strong>
                    <small>{Math.round((t.progress || 0) * 100)}% complete</small>
                  </div>
                  <span className={`status-badge ${statusClass(t.status)}`}>{t.status}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="ops-section">
            <div className="ops-section-header"><h2><Plane size={16} /> Flight Telemetry</h2></div>
            {selected ? (
              <div className="tracking-telemetry">
                <div><span>Flight</span><strong>#{selected.flightId}</strong></div>
                <div><span>Route</span><strong>{selected.route}</strong></div>
                <div><span>Status</span><strong>{selected.status}</strong></div>
                <div><span>Progress</span><strong>{Math.round((selected.progress || 0) * 100)}%</strong></div>
                <div><span>Altitude</span><strong>{selected.altitudeFeet || 0} ft</strong></div>
                <div><span>Ground Speed</span><strong>{selected.groundSpeedKts || 0} kts</strong></div>
                <div><span>Current Position</span><strong>{selected.currentLat?.toFixed(2)}, {selected.currentLon?.toFixed(2)}</strong></div>
              </div>
            ) : (
              <p className="text-muted">Click a moving aircraft on the globe to inspect details.</p>
            )}
          </div>
        </aside>
      </div>
    </Layout>
  );
}
