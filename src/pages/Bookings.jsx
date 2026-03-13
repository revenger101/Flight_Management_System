import { useEffect, useMemo, useState } from 'react';
import Layout from '../components/Layout';
import { BookOpen, Plus, Trash2, X, RotateCcw } from 'lucide-react';
import { bookingService } from '../services/bookingService';
import { passengerService } from '../services/passengerService';
import { flightService } from '../services/flightService';
import toast from 'react-hot-toast';

const emptyForm = { kind: 'One-way', date: '', type: 'ECONOMIC', passengerId: '', flightId: '' };

export default function Bookings() {
  const [bookings, setBookings] = useState([]);
  const [passengers, setPassengers] = useState([]);
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({
    q: '',
    passengerId: 'all',
    flightId: 'all',
    type: 'all',
    kind: 'all',
    fromDate: '',
    toDate: '',
    sortBy: 'date-desc'
  });

  const load = () => {
    setLoading(true);
    Promise.all([bookingService.getAll(), passengerService.getAll(), flightService.getAll()])
      .then(([bo, pa, fl]) => { setBookings(bo.data); setPassengers(pa.data); setFlights(fl.data); })
      .catch(() => toast.error('Failed'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const getPassenger = (id) => passengers.find(p => p.id === id);
  const getFlight = (id) => flights.find(f => f.id === id);

  const filteredBookings = useMemo(() => {
    const q = filters.q.trim().toLowerCase();

    let rows = bookings.filter((b) => {
      const p = passengers.find((x) => x.id === b.passengerId);
      const f = flights.find((x) => x.id === b.flightId);

      const textOk = !q || [
        String(b.id || ''),
        b.kind,
        b.type,
        p?.name,
        String(b.flightId || ''),
        String(f?.miles || '')
      ].some((v) => (v || '').toLowerCase().includes(q));

      const passengerOk = filters.passengerId === 'all' || String(b.passengerId || '') === filters.passengerId;
      const flightOk = filters.flightId === 'all' || String(b.flightId || '') === filters.flightId;
      const typeOk = filters.type === 'all' || b.type === filters.type;
      const kindOk = filters.kind === 'all' || b.kind === filters.kind;
      const fromOk = !filters.fromDate || (b.date || '') >= filters.fromDate;
      const toOk = !filters.toDate || (b.date || '') <= filters.toDate;

      return textOk && passengerOk && flightOk && typeOk && kindOk && fromOk && toOk;
    });

    rows = [...rows].sort((a, b) => {
      if (filters.sortBy === 'date-asc') return (a.date || '').localeCompare(b.date || '');
      if (filters.sortBy === 'id-asc') return Number(a.id || 0) - Number(b.id || 0);
      if (filters.sortBy === 'id-desc') return Number(b.id || 0) - Number(a.id || 0);
      return (b.date || '').localeCompare(a.date || '');
    });

    return rows;
  }, [bookings, passengers, flights, filters]);

  const resetFilters = () => setFilters({
    q: '',
    passengerId: 'all',
    flightId: 'all',
    type: 'all',
    kind: 'all',
    fromDate: '',
    toDate: '',
    sortBy: 'date-desc'
  });

  const handleSubmit = async () => {
    if (!form.passengerId || !form.flightId || !form.date) return toast.error('All fields required');
    try {
      await bookingService.create({ ...form, passengerId: parseInt(form.passengerId), flightId: parseInt(form.flightId) });
      toast.success('Booking created!');
      setShowModal(false); load();
    } catch { toast.error('Failed'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this booking?')) return;
    try { await bookingService.delete(id); toast.success('Deleted'); load(); }
    catch { toast.error('Delete failed'); }
  };

  return (
    <Layout title="Bookings" subtitle="Manage all flight reservations">
      <div className="page-header">
        <div>
          <h1 className="page-title">Bookings</h1>
          <p className="page-subtitle">{bookings.length} reservation(s) total</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setForm(emptyForm); setShowModal(true); }}>
          <Plus size={16} /> New Booking
        </button>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">All Reservations</span></div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              className="filter-input"
              placeholder="Search by booking ID, passenger, type..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.passengerId}
              onChange={(e) => setFilters({ ...filters, passengerId: e.target.value })}
            >
              <option value="all">Passenger: All</option>
              {passengers.map(p => <option key={p.id} value={String(p.id)}>{p.name}</option>)}
            </select>
            <select
              className="filter-select"
              value={filters.flightId}
              onChange={(e) => setFilters({ ...filters, flightId: e.target.value })}
            >
              <option value="all">Flight: All</option>
              {flights.map(f => <option key={f.id} value={String(f.id)}>Flight #{f.id}</option>)}
            </select>
            <select
              className="filter-select"
              value={filters.type}
              onChange={(e) => setFilters({ ...filters, type: e.target.value })}
            >
              <option value="all">Type: All</option>
              <option value="ECONOMIC">ECONOMIC</option>
              <option value="BUSINESS">BUSINESS</option>
            </select>
            <select
              className="filter-select"
              value={filters.kind}
              onChange={(e) => setFilters({ ...filters, kind: e.target.value })}
            >
              <option value="all">Kind: All</option>
              <option value="One-way">One-way</option>
              <option value="Round-trip">Round-trip</option>
            </select>
            <input
              className="filter-input"
              type="date"
              value={filters.fromDate}
              onChange={(e) => setFilters({ ...filters, fromDate: e.target.value })}
            />
            <input
              className="filter-input"
              type="date"
              value={filters.toDate}
              onChange={(e) => setFilters({ ...filters, toDate: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.sortBy}
              onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
            >
              <option value="date-desc">Sort: Date newest</option>
              <option value="date-asc">Sort: Date oldest</option>
              <option value="id-desc">Sort: ID desc</option>
              <option value="id-asc">Sort: ID asc</option>
            </select>
            <button className="btn btn-secondary" onClick={resetFilters}>
              <RotateCcw size={14} /> Reset
            </button>
          </div>
          <div className="filters-meta">
            <span className="filter-chip">Showing <strong>{filteredBookings.length}</strong> / {bookings.length}</span>
            {(filters.type !== 'all' || filters.kind !== 'all') && <span className="filter-chip">Class filters active</span>}
          </div>
        </div>
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Passenger</th><th>Flight</th><th>Type</th><th>Kind</th><th>Date</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredBookings.length === 0 ? (
                <tr><td colSpan={7}><div className="table-empty">No booking matches your current filters.</div></td></tr>
              ) : filteredBookings.map(b => {
                const p = getPassenger(b.passengerId);
                const f = getFlight(b.flightId);
                return (
                  <tr key={b.id}>
                    <td>#{b.id}</td>
                    <td>{p?.name ?? `Passenger #${b.passengerId}`}</td>
                    <td>Flight #{b.flightId}{f ? ` (${f.miles}mi)` : ''}</td>
                    <td><span className={`badge ${b.type === 'BUSINESS' ? 'badge-gold' : 'badge-blue'}`}>{b.type}</span></td>
                    <td>{b.kind}</td>
                    <td style={{ color: 'var(--text-muted)' }}>{b.date}</td>
                    <td>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}><Trash2 size={13} /></button>
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
              <span className="modal-title">New Booking</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Passenger</label>
                <select className="form-select" value={form.passengerId} onChange={e => setForm({ ...form, passengerId: e.target.value })}>
                  <option value="">Select passenger</option>
                  {passengers.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Flight</label>
                <select className="form-select" value={form.flightId} onChange={e => setForm({ ...form, flightId: e.target.value })}>
                  <option value="">Select flight</option>
                  {flights.map(f => <option key={f.id} value={f.id}>Flight #{f.id} — {f.miles} miles</option>)}
                </select>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Type</label>
                  <select className="form-select" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
                    <option value="ECONOMIC">ECONOMIC</option>
                    <option value="BUSINESS">BUSINESS</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Kind</label>
                  <select className="form-select" value={form.kind} onChange={e => setForm({ ...form, kind: e.target.value })}>
                    <option>One-way</option>
                    <option>Round-trip</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Date</label>
                <input className="form-input" type="date" value={form.date} onChange={e => setForm({ ...form, date: e.target.value })} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmit}>Create Booking</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}