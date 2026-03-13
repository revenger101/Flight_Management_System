import { useEffect, useMemo, useState } from 'react';
import Layout from '../components/Layout';
import { Building2, Plus, Pencil, Trash2, X, RotateCcw } from 'lucide-react';
import { airportService } from '../services/airportService';
import { airlineService } from '../services/airlineService';
import toast from 'react-hot-toast';

const emptyForm = { name: '', shortName: '', country: '', fee: '', airlineId: '' };

export default function Airports() {
  const [airports, setAirports] = useState([]);
  const [airlines, setAirlines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);
  const [filters, setFilters] = useState({
    q: '',
    country: '',
    airlineId: 'all',
    minFee: '',
    maxFee: '',
    sortBy: 'name-asc'
  });

  const load = () => {
    setLoading(true);
    Promise.all([airportService.getAll(), airlineService.getAll()])
      .then(([ap, al]) => { setAirports(ap.data); setAirlines(al.data); })
      .catch(() => toast.error('Failed to load'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };
  const openEdit = (a) => {
    setForm({ name: a.name, shortName: a.shortName, country: a.country, fee: a.fee, airlineId: a.airlineId || '' });
    setEditId(a.id); setShowModal(true);
  };

  const handleSubmit = async () => {
    if (!form.name || !form.shortName) return toast.error('Name and Short Name required');
    try {
      const payload = { ...form, fee: parseFloat(form.fee) || 0, airlineId: form.airlineId || null };
      if (editId) { await airportService.update(editId, payload); toast.success('Airport updated!'); }
      else { await airportService.create(payload); toast.success('Airport created!'); }
      setShowModal(false); load();
    } catch { toast.error('Operation failed'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this airport?')) return;
    try { await airportService.delete(id); toast.success('Deleted'); load(); }
    catch { toast.error('Delete failed'); }
  };

  const getAirlineName = (id) => airlines.find(a => a.id === id)?.name || '—';

  const filteredAirports = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    const country = filters.country.trim().toLowerCase();
    const minFee = filters.minFee === '' ? null : Number(filters.minFee);
    const maxFee = filters.maxFee === '' ? null : Number(filters.maxFee);

    let rows = airports.filter((a) => {
      const textOk = !q || [a.name, a.shortName, a.country].some((v) => (v || '').toLowerCase().includes(q));
      const countryOk = !country || (a.country || '').toLowerCase().includes(country);
      const airlineOk = filters.airlineId === 'all' || String(a.airlineId || '') === filters.airlineId;
      const fee = Number(a.fee || 0);
      const minOk = minFee === null || fee >= minFee;
      const maxOk = maxFee === null || fee <= maxFee;
      return textOk && countryOk && airlineOk && minOk && maxOk;
    });

    rows = [...rows].sort((a, b) => {
      if (filters.sortBy === 'name-desc') return (b.name || '').localeCompare(a.name || '');
      if (filters.sortBy === 'fee-asc') return Number(a.fee || 0) - Number(b.fee || 0);
      if (filters.sortBy === 'fee-desc') return Number(b.fee || 0) - Number(a.fee || 0);
      if (filters.sortBy === 'id-desc') return (b.id || 0) - (a.id || 0);
      return (a.name || '').localeCompare(b.name || '');
    });

    return rows;
  }, [airports, filters]);

  const resetFilters = () => setFilters({ q: '', country: '', airlineId: 'all', minFee: '', maxFee: '', sortBy: 'name-asc' });

  return (
    <Layout title="Airports" subtitle="Manage airport facilities">
      <div className="page-header">
        <div>
          <h1 className="page-title">Airports</h1>
          <p className="page-subtitle">{airports.length} airport(s) registered</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Airport</button>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">All Airports</span></div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              className="filter-input"
              placeholder="Search name, code, country..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <input
              className="filter-input"
              placeholder="Country contains..."
              value={filters.country}
              onChange={(e) => setFilters({ ...filters, country: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.airlineId}
              onChange={(e) => setFilters({ ...filters, airlineId: e.target.value })}
            >
              <option value="all">Airline: All</option>
              <option value="">Airline: None</option>
              {airlines.map(a => <option key={a.id} value={String(a.id)}>{a.name}</option>)}
            </select>
            <input
              className="filter-input"
              type="number"
              placeholder="Min fee"
              value={filters.minFee}
              onChange={(e) => setFilters({ ...filters, minFee: e.target.value })}
            />
            <input
              className="filter-input"
              type="number"
              placeholder="Max fee"
              value={filters.maxFee}
              onChange={(e) => setFilters({ ...filters, maxFee: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.sortBy}
              onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
            >
              <option value="name-asc">Sort: Name A-Z</option>
              <option value="name-desc">Sort: Name Z-A</option>
              <option value="fee-asc">Sort: Fee low-high</option>
              <option value="fee-desc">Sort: Fee high-low</option>
              <option value="id-desc">Sort: Newest first</option>
            </select>
            <button className="btn btn-secondary" onClick={resetFilters}>
              <RotateCcw size={14} /> Reset
            </button>
          </div>
          <div className="filters-meta">
            <span className="filter-chip">Showing <strong>{filteredAirports.length}</strong> / {airports.length}</span>
            {filters.airlineId !== 'all' && <span className="filter-chip">Airline filter active</span>}
          </div>
        </div>
        {loading ? <div className="loading-screen"><div className="spinner" /></div> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>Code</th><th>Country</th><th>Fee</th><th>Airline</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredAirports.length === 0 ? (
                <tr><td colSpan={7}><div className="table-empty">No airport matches your current filters.</div></td></tr>
              ) : filteredAirports.map(a => (
                <tr key={a.id}>
                  <td>#{a.id}</td>
                  <td>{a.name}</td>
                  <td><span className="badge badge-blue">{a.shortName}</span></td>
                  <td>{a.country}</td>
                  <td style={{ color: 'var(--gold)' }}>${a.fee}</td>
                  <td>{getAirlineName(a.airlineId)}</td>
                  <td>
                    <div className="action-buttons">
                      <button className="btn btn-secondary btn-sm" onClick={() => openEdit(a)}><Pencil size={13} /></button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a.id)}><Trash2 size={13} /></button>
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
              <span className="modal-title">{editId ? 'Edit Airport' : 'New Airport'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Airport Name *</label>
                  <input className="form-input" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Charles de Gaulle" />
                </div>
                <div className="form-group">
                  <label className="form-label">IATA Code *</label>
                  <input className="form-input" value={form.shortName} onChange={e => setForm({ ...form, shortName: e.target.value })} placeholder="CDG" />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Country</label>
                  <input className="form-input" value={form.country} onChange={e => setForm({ ...form, country: e.target.value })} placeholder="France" />
                </div>
                <div className="form-group">
                  <label className="form-label">Fee ($)</label>
                  <input className="form-input" type="number" value={form.fee} onChange={e => setForm({ ...form, fee: e.target.value })} placeholder="30" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Airline</label>
                <select className="form-select" value={form.airlineId} onChange={e => setForm({ ...form, airlineId: e.target.value })}>
                  <option value="">None</option>
                  {airlines.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmit}>{editId ? 'Save Changes' : 'Create Airport'}</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}