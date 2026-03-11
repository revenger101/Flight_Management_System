import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { Users, Plus, Pencil, Trash2, X } from 'lucide-react';
import { passengerService } from '../services/passengerService';
import toast from 'react-hot-toast';

const emptyForm = {
  name: '', cc: '', mileCard: '', status: 'Silver',
  milesAccount: { number: '', flightMiles: 0, statusMiles: 0 }
};

export default function Passengers() {
  const [passengers, setPassengers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);

  const load = () => {
    setLoading(true);
    passengerService.getAll().then(r => setPassengers(r.data)).catch(() => toast.error('Failed')).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };
  const openEdit = (p) => {
    setForm({ name: p.name, cc: p.cc, mileCard: p.mileCard, status: p.status,
      milesAccount: p.milesAccount || { number: '', flightMiles: 0, statusMiles: 0 } });
    setEditId(p.id); setShowModal(true);
  };

  const handleSubmit = async () => {
    if (!form.name) return toast.error('Name is required');
    try {
      if (editId) { await passengerService.update(editId, form); toast.success('Updated!'); }
      else { await passengerService.create(form); toast.success('Created!'); }
      setShowModal(false); load();
    } catch { toast.error('Failed'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this passenger?')) return;
    try { await passengerService.delete(id); toast.success('Deleted'); load(); }
    catch { toast.error('Delete failed'); }
  };

  const statusColor = (s) => {
    if (s === 'Gold') return 'badge-gold';
    if (s === 'Silver') return 'badge-blue';
    return 'badge-green';
  };

  return (
    <Layout title="Passengers" subtitle="Manage registered passengers">
      <div className="page-header">
        <div>
          <h1 className="page-title">Passengers</h1>
          <p className="page-subtitle">{passengers.length} passenger(s) registered</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Passenger</button>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">All Passengers</span></div>
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>CC</th><th>Mile Card</th><th>Status</th><th>Flight Miles</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {passengers.map(p => (
                <tr key={p.id}>
                  <td>#{p.id}</td>
                  <td>{p.name}</td>
                  <td style={{ color: 'var(--text-muted)', fontFamily: 'monospace' }}>{p.cc}</td>
                  <td>{p.mileCard}</td>
                  <td><span className={`badge ${statusColor(p.status)}`}>{p.status}</span></td>
                  <td style={{ color: 'var(--gold)' }}>{p.milesAccount?.flightMiles ?? 0} mi</td>
                  <td>
                    <div className="action-buttons">
                      <button className="btn btn-secondary btn-sm" onClick={() => openEdit(p)}><Pencil size={13} /></button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.id)}><Trash2 size={13} /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">{editId ? 'Edit Passenger' : 'New Passenger'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Full Name *</label>
                  <input className="form-input" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Alice Dupont" />
                </div>
                <div className="form-group">
                  <label className="form-label">CC (ID)</label>
                  <input className="form-input" value={form.cc} onChange={e => setForm({ ...form, cc: e.target.value })} placeholder="FR123456" />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Mile Card</label>
                  <input className="form-input" value={form.mileCard} onChange={e => setForm({ ...form, mileCard: e.target.value })} placeholder="MC001" />
                </div>
                <div className="form-group">
                  <label className="form-label">Status</label>
                  <select className="form-select" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                    <option>Bronze</option><option>Silver</option><option>Gold</option><option>Platinum</option>
                  </select>
                </div>
              </div>
              {!editId && <>
                <div style={{ borderTop: '1px solid var(--border)', paddingTop: 16, marginTop: 4, marginBottom: 12, fontSize: 12, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontWeight: 600 }}>Miles Account</div>
                <div className="form-row">
                  <div className="form-group">
                    <label className="form-label">Account Number</label>
                    <input className="form-input" value={form.milesAccount.number}
                      onChange={e => setForm({ ...form, milesAccount: { ...form.milesAccount, number: e.target.value } })} placeholder="MA001" />
                  </div>
                  <div className="form-group">
                    <label className="form-label">Flight Miles</label>
                    <input className="form-input" type="number" value={form.milesAccount.flightMiles}
                      onChange={e => setForm({ ...form, milesAccount: { ...form.milesAccount, flightMiles: parseInt(e.target.value) } })} />
                  </div>
                </div>
              </>}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmit}>{editId ? 'Save Changes' : 'Create Passenger'}</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}