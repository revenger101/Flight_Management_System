import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { Plane, Plus, Pencil, Trash2, X } from 'lucide-react';
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
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Time</th><th>From</th><th>To</th><th>Miles</th><th>Handlings</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {flights.map(f => {
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