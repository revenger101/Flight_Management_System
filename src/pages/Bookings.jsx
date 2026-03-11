import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { BookOpen, Plus, Trash2, X } from 'lucide-react';
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
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Passenger</th><th>Flight</th><th>Type</th><th>Kind</th><th>Date</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {bookings.map(b => {
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