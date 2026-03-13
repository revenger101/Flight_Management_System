import { useEffect, useMemo, useState } from 'react';
import Layout from '../components/Layout';
import { Plane, Plus, Pencil, Trash2, X, RotateCcw } from 'lucide-react';
import { flightService } from '../services/flightService';
import { airportService } from '../services/airportService';
import toast from 'react-hot-toast';

const emptyForm = { time: '', miles: '', departureAirportId: '', arrivalAirportId: '' };

export default function Flights() {
  const [flights, setFlights] = useState([]);
  const [airports, setAirports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);
  const [filters, setFilters] = useState({
    q: '',
    departureAirportId: 'all',
    arrivalAirportId: 'all',
    minMiles: '',
    maxMiles: '',
    startTime: '',
    endTime: '',
    minHandlings: '',
    sortBy: 'id-desc'
  });

  const load = () => {
    setLoading(true);
    Promise.all([flightService.getAll(), airportService.getAll()])
      .then(([fl, ap]) => { setFlights(fl.data); setAirports(ap.data); })
      .catch(() => toast.error('Failed'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const getAirport = (id) => airports.find(a => a.id === id);
  const openCreate = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };
  const openEdit = (f) => {
    setForm({ time: f.time, miles: f.miles, departureAirportId: f.departureAirportId, arrivalAirportId: f.arrivalAirportId });
    setEditId(f.id); setShowModal(true);
  };

  const handleSubmit = async () => {
    if (!form.departureAirportId || !form.arrivalAirportId) return toast.error('Select both airports');
    try {
      const payload = { ...form, miles: parseInt(form.miles) || 0 };
      if (editId) { await flightService.update(editId, payload); toast.success('Updated!'); }
      else { await flightService.create(payload); toast.success('Flight created!'); }
      setShowModal(false); load();
    } catch { toast.error('Failed'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this flight?')) return;
    try { await flightService.delete(id); toast.success('Deleted'); load(); }
    catch { toast.error('Delete failed'); }
  };

  const filteredFlights = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    const minMiles = filters.minMiles === '' ? null : Number(filters.minMiles);
    const maxMiles = filters.maxMiles === '' ? null : Number(filters.maxMiles);
    const minHandlings = filters.minHandlings === '' ? null : Number(filters.minHandlings);

    let rows = flights.filter((f) => {
      const dep = airports.find((a) => a.id === f.departureAirportId);
      const arr = airports.find((a) => a.id === f.arrivalAirportId);

      const textOk = !q || [
        String(f.id || ''),
        dep?.name,
        dep?.shortName,
        arr?.name,
        arr?.shortName
      ].some((v) => (v || '').toLowerCase().includes(q));

      const depOk = filters.departureAirportId === 'all' || String(f.departureAirportId || '') === filters.departureAirportId;
      const arrOk = filters.arrivalAirportId === 'all' || String(f.arrivalAirportId || '') === filters.arrivalAirportId;

      const miles = Number(f.miles || 0);
      const minMilesOk = minMiles === null || miles >= minMiles;
      const maxMilesOk = maxMiles === null || miles <= maxMiles;

      const flightTime = f.time || '';
      const startOk = !filters.startTime || flightTime >= filters.startTime;
      const endOk = !filters.endTime || flightTime <= filters.endTime;

      const handlingCount = Number(f.flightHandlings?.length || 0);
      const handlingsOk = minHandlings === null || handlingCount >= minHandlings;

      return textOk && depOk && arrOk && minMilesOk && maxMilesOk && startOk && endOk && handlingsOk;
    });

    rows = [...rows].sort((a, b) => {
      if (filters.sortBy === 'miles-asc') return Number(a.miles || 0) - Number(b.miles || 0);
      if (filters.sortBy === 'miles-desc') return Number(b.miles || 0) - Number(a.miles || 0);
      if (filters.sortBy === 'time-asc') return (a.time || '').localeCompare(b.time || '');
      if (filters.sortBy === 'time-desc') return (b.time || '').localeCompare(a.time || '');
      return Number(b.id || 0) - Number(a.id || 0);
    });

    return rows;
  }, [flights, airports, filters]);

  const resetFilters = () => setFilters({
    q: '',
    departureAirportId: 'all',
    arrivalAirportId: 'all',
    minMiles: '',
    maxMiles: '',
    startTime: '',
    endTime: '',
    minHandlings: '',
    sortBy: 'id-desc'
  });

  return (
    <Layout title="Flights" subtitle="Manage all flight routes">
      <div className="page-header">
        <div>
          <h1 className="page-title">Flights</h1>
          <p className="page-subtitle">{flights.length} flight(s) scheduled</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Flight</button>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">All Flights</span></div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              className="filter-input"
              placeholder="Search by ID or airport..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.departureAirportId}
              onChange={(e) => setFilters({ ...filters, departureAirportId: e.target.value })}
            >
              <option value="all">From: All airports</option>
              {airports.map(a => <option key={a.id} value={String(a.id)}>{a.shortName} - {a.name}</option>)}
            </select>
            <select
              className="filter-select"
              value={filters.arrivalAirportId}
              onChange={(e) => setFilters({ ...filters, arrivalAirportId: e.target.value })}
            >
              <option value="all">To: All airports</option>
              {airports.map(a => <option key={a.id} value={String(a.id)}>{a.shortName} - {a.name}</option>)}
            </select>
            <input
              className="filter-input"
              type="number"
              placeholder="Min miles"
              value={filters.minMiles}
              onChange={(e) => setFilters({ ...filters, minMiles: e.target.value })}
            />
            <input
              className="filter-input"
              type="number"
              placeholder="Max miles"
              value={filters.maxMiles}
              onChange={(e) => setFilters({ ...filters, maxMiles: e.target.value })}
            />
            <input
              className="filter-input"
              type="time"
              value={filters.startTime}
              onChange={(e) => setFilters({ ...filters, startTime: e.target.value })}
            />
            <input
              className="filter-input"
              type="time"
              value={filters.endTime}
              onChange={(e) => setFilters({ ...filters, endTime: e.target.value })}
            />
            <input
              className="filter-input"
              type="number"
              placeholder="Min handlings"
              value={filters.minHandlings}
              onChange={(e) => setFilters({ ...filters, minHandlings: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.sortBy}
              onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
            >
              <option value="id-desc">Sort: Newest first</option>
              <option value="miles-desc">Sort: Miles high-low</option>
              <option value="miles-asc">Sort: Miles low-high</option>
              <option value="time-asc">Sort: Time asc</option>
              <option value="time-desc">Sort: Time desc</option>
            </select>
            <button className="btn btn-secondary" onClick={resetFilters}>
              <RotateCcw size={14} /> Reset
            </button>
          </div>
          <div className="filters-meta">
            <span className="filter-chip">Showing <strong>{filteredFlights.length}</strong> / {flights.length}</span>
            {(filters.departureAirportId !== 'all' || filters.arrivalAirportId !== 'all') && (
              <span className="filter-chip">Route filter active</span>
            )}
          </div>
        </div>
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Time</th><th>From</th><th>To</th><th>Miles</th><th>Handlings</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredFlights.length === 0 ? (
                <tr><td colSpan={7}><div className="table-empty">No flight matches your current filters.</div></td></tr>
              ) : filteredFlights.map(f => {
                const dep = getAirport(f.departureAirportId);
                const arr = getAirport(f.arrivalAirportId);
                return (
                  <tr key={f.id}>
                    <td>#{f.id}</td>
                    <td style={{ fontFamily: 'monospace' }}>{f.time}</td>
                    <td>
                      {dep ? <span className="badge badge-blue">{dep.shortName}</span> : `#${f.departureAirportId}`}
                    </td>
                    <td>
                      {arr ? <span className="badge badge-gold">{arr.shortName}</span> : `#${f.arrivalAirportId}`}
                    </td>
                    <td style={{ color: 'var(--gold)' }}>{f.miles?.toLocaleString()} mi</td>
                    <td><span className="badge badge-green">{f.flightHandlings?.length ?? 0}</span></td>
                    <td>
                      <div className="action-buttons">
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(f)}><Pencil size={13} /></button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(f.id)}><Trash2 size={13} /></button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">{editId ? 'Edit Flight' : 'New Flight'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Departure Time</label>
                  <input className="form-input" type="time" value={form.time} onChange={e => setForm({ ...form, time: e.target.value })} />
                </div>
                <div className="form-group">
                  <label className="form-label">Distance (miles)</label>
                  <input className="form-input" type="number" value={form.miles} onChange={e => setForm({ ...form, miles: e.target.value })} placeholder="3500" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Departure Airport</label>
                <select className="form-select" value={form.departureAirportId} onChange={e => setForm({ ...form, departureAirportId: e.target.value })}>
                  <option value="">Select departure</option>
                  {airports.map(a => <option key={a.id} value={a.id}>{a.name} ({a.shortName})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Arrival Airport</label>
                <select className="form-select" value={form.arrivalAirportId} onChange={e => setForm({ ...form, arrivalAirportId: e.target.value })}>
                  <option value="">Select arrival</option>
                  {airports.map(a => <option key={a.id} value={a.id}>{a.name} ({a.shortName})</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmit}>{editId ? 'Save Changes' : 'Create Flight'}</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}