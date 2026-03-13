import { useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import RoleGate from '../components/RoleGate';
import { Users, Plus, Pencil, Trash2, X, RotateCcw, FileSpreadsheet, FileDown, Download } from 'lucide-react';
import { passengerService } from '../services/passengerService';
import toast from 'react-hot-toast';
import { usePagination } from '../hooks/usePagination';
import PaginationControls from '../components/PaginationControls';
import { exportTablePdf, exportToCsv, exportToExcel } from '../utils/exportUtils';
import { getNextTierProgress, getStatusForMiles } from '../utils/loyalty';
import { usePassengersQuery } from '../hooks/queries';
import { useFilterPresets } from '../hooks/useFilterPresets';
import { usePageShortcuts } from '../hooks/usePageShortcuts';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';
import { getErrorMessage } from '../utils/errorUtils';
import { useAuth } from '../contexts/AuthContext';

const emptyForm = {
  name: '', cc: '', mileCard: '', status: 'Silver',
  milesAccount: { number: '', flightMiles: 0, statusMiles: 0 }
};

const defaultFilters = {
  q: '',
  status: 'all',
  minMiles: '',
  maxMiles: '',
  hasMileCard: 'all',
  sortBy: 'miles-desc'
};

export default function Passengers() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const { data: passengers = [], isLoading: loading, refetch } = usePassengersQuery();
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [editId, setEditId] = useState(null);
  const [presetName, setPresetName] = useState('');
  const searchInputRef = useRef(null);
  const {
    filters,
    setFilters,
    resetFilters,
    presets,
    savePreset,
    applyPreset,
  } = useFilterPresets('passengers', defaultFilters);

  const requireAdmin = () => {
    if (isAdmin) {
      return true;
    }
    toast.error('Admin access required for passenger changes');
    return false;
  };

  const openCreate = () => {
    if (!requireAdmin()) {
      return;
    }
    setForm(emptyForm); setEditId(null); setShowModal(true);
  };

  usePageShortcuts({
    onCreate: openCreate,
    onSearch: () => searchInputRef.current?.focus(),
    onCloseModal: () => setShowModal(false),
    modalOpen: showModal,
  });

  const openEdit = (p) => {
    if (!requireAdmin()) {
      return;
    }
    setForm({ name: p.name, cc: p.cc, mileCard: p.mileCard, status: p.status,
      milesAccount: p.milesAccount || { number: '', flightMiles: 0, statusMiles: 0 } });
    setEditId(p.id); setShowModal(true);
  };

  const handleSubmit = async () => {
    if (!requireAdmin()) {
      return;
    }
    if (!form.name) return toast.error('Name is required');
    try {
      const autoStatus = getStatusForMiles(form.milesAccount?.flightMiles || 0);
      const payload = { ...form, status: autoStatus };
      if (editId) { await passengerService.update(editId, payload); toast.success(`Updated! Auto status: ${autoStatus}`); }
      else { await passengerService.create(payload); toast.success(`Created! Auto status: ${autoStatus}`); }
      setShowModal(false); refetch();
    } catch (error) { toast.error(getErrorMessage(error, 'Failed')); }
  };

  const handleDelete = async (id) => {
    if (!requireAdmin()) {
      return;
    }
    if (!confirm('Delete this passenger?')) return;
    try { await passengerService.delete(id); toast.success('Deleted'); refetch(); }
    catch (error) { toast.error(getErrorMessage(error, 'Delete failed')); }
  };

  const statusColor = (s) => {
    if (s === 'Gold') return 'badge-gold';
    if (s === 'Silver') return 'badge-blue';
    return 'badge-green';
  };

  const passengersWithLoyalty = useMemo(() => {
    return passengers.map((p) => {
      const miles = Number(p.milesAccount?.flightMiles || 0);
      return {
        ...p,
        autoStatus: getStatusForMiles(miles),
        loyaltyProgress: getNextTierProgress(miles),
      };
    });
  }, [passengers]);

  const filteredPassengers = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    const minMiles = filters.minMiles === '' ? null : Number(filters.minMiles);
    const maxMiles = filters.maxMiles === '' ? null : Number(filters.maxMiles);

    let rows = passengersWithLoyalty.filter((p) => {
      const textOk = !q || [p.name, p.cc, p.mileCard, p.milesAccount?.number]
        .some((v) => (v || '').toLowerCase().includes(q));
      const statusOk = filters.status === 'all' || (p.autoStatus || '') === filters.status;
      const hasCard = !!(p.mileCard || '').trim();
      const cardOk = filters.hasMileCard === 'all' ? true : filters.hasMileCard === 'yes' ? hasCard : !hasCard;
      const miles = Number(p.milesAccount?.flightMiles || 0);
      const minOk = minMiles === null || miles >= minMiles;
      const maxOk = maxMiles === null || miles <= maxMiles;
      return textOk && statusOk && cardOk && minOk && maxOk;
    });

    rows = [...rows].sort((a, b) => {
      const milesA = Number(a.milesAccount?.flightMiles || 0);
      const milesB = Number(b.milesAccount?.flightMiles || 0);
      if (filters.sortBy === 'miles-asc') return milesA - milesB;
      if (filters.sortBy === 'name-asc') return (a.name || '').localeCompare(b.name || '');
      if (filters.sortBy === 'name-desc') return (b.name || '').localeCompare(a.name || '');
      return milesB - milesA;
    });

    return rows;
  }, [passengersWithLoyalty, filters]);

  const {
    page,
    pageSize,
    totalPages,
    totalItems,
    rangeStart,
    rangeEnd,
    paginatedItems,
    setPage,
    setPageSize,
  } = usePagination(filteredPassengers, 10);

  const exportRows = filteredPassengers.map((p) => ({
    ID: p.id,
    Name: p.name,
    CC: p.cc,
    MileCard: p.mileCard,
    Status: p.autoStatus,
    NextTier: p.loyaltyProgress.nextTier || 'Max',
    NextTierProgress: `${p.loyaltyProgress.progressPct}%`,
    FlightMiles: p.milesAccount?.flightMiles || 0,
  }));

  const automationPreview = getNextTierProgress(form.milesAccount?.flightMiles || 0);

  return (
    <Layout title="Passengers" subtitle="Manage registered passengers">
      <div className="page-header">
        <div>
          <h1 className="page-title">Passengers</h1>
          <p className="page-subtitle">{passengers.length} passenger(s) registered</p>
        </div>
        <RoleGate
          allowedRoles={['ADMIN']}
          fallback={<span className="page-role-note">Admin role required to manage passenger records.</span>}
        >
          <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Passenger</button>
        </RoleGate>
      </div>

      <div className="table-container">
        <div className="table-header">
          <span className="table-title">All Passengers</span>
          <div className="table-tools">
            <button className="btn btn-secondary btn-sm" onClick={() => exportToCsv('passengers', exportRows)}><Download size={13} /> CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportToExcel('passengers', exportRows, 'Passengers')}><FileSpreadsheet size={13} /> Excel</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportTablePdf('passengers', 'Passengers Report', exportRows)}><FileDown size={13} /> PDF</button>
          </div>
        </div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              ref={searchInputRef}
              className="filter-input"
              placeholder="Search by name, CC, mile card..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <option value="all">Status: All</option>
              <option value="Bronze">Bronze</option>
              <option value="Silver">Silver</option>
              <option value="Gold">Gold</option>
              <option value="Platinum">Platinum</option>
            </select>
            <select
              className="filter-select"
              value={filters.hasMileCard}
              onChange={(e) => setFilters({ ...filters, hasMileCard: e.target.value })}
            >
              <option value="all">Mile card: All</option>
              <option value="yes">With mile card</option>
              <option value="no">Without mile card</option>
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
            <select
              className="filter-select"
              value={filters.sortBy}
              onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
            >
              <option value="miles-desc">Sort: Miles high-low</option>
              <option value="miles-asc">Sort: Miles low-high</option>
              <option value="name-asc">Sort: Name A-Z</option>
              <option value="name-desc">Sort: Name Z-A</option>
            </select>
            <button className="btn btn-secondary" onClick={resetFilters}>
              <RotateCcw size={14} /> Reset
            </button>
          </div>
          <div className="filters-meta">
            <span className="filter-chip">Showing <strong>{filteredPassengers.length}</strong> / {passengers.length}</span>
            {filters.status !== 'all' && <span className="filter-chip">Status: <strong>{filters.status}</strong></span>}
          </div>
          <div className="filter-presets">
            <input
              className="filter-input"
              placeholder="Preset name"
              value={presetName}
              onChange={(e) => setPresetName(e.target.value)}
            />
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => {
                const saved = savePreset(presetName);
                if (saved.ok) {
                  toast.success('Preset saved');
                  setPresetName('');
                } else {
                  toast.error(saved.reason);
                }
              }}
            >Save Preset</button>
            <select className="filter-select" defaultValue="" onChange={(e) => e.target.value && applyPreset(e.target.value)}>
              <option value="">Apply preset</option>
              {presets.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
        </div>
        {loading ? <TableSkeleton rows={9} cols={8} /> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>CC</th><th>Mile Card</th><th>Status</th><th>Loyalty Progress</th><th>Flight Miles</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredPassengers.length === 0 ? (
                <tr><td colSpan={8}>
                  <EmptyState
                    icon={<Users size={32} />}
                    title={passengers.length === 0 ? 'No passengers yet' : 'No passenger matches your filters'}
                    description={passengers.length === 0 ? 'Create your first passenger profile.' : 'Try another filter combination or add a passenger.'}
                    ctaLabel={passengers.length === 0 ? (isAdmin ? 'Create Passenger' : null) : 'Reset Filters'}
                    onCta={passengers.length === 0 ? (isAdmin ? openCreate : null) : resetFilters}
                  />
                </td></tr>
              ) : paginatedItems.map(p => (
                <tr key={p.id}>
                  <td><Link to={`/passengers/${p.id}`} className="table-link">#{p.id}</Link></td>
                  <td><Link to={`/passengers/${p.id}`} className="table-link">{p.name}</Link></td>
                  <td style={{ color: 'var(--text-muted)', fontFamily: 'monospace' }}>{p.cc}</td>
                  <td>{p.mileCard}</td>
                  <td><span className={`badge ${statusColor(p.autoStatus)}`}>{p.autoStatus}</span></td>
                  <td>
                    {p.loyaltyProgress.nextTier ? (
                      <div className="loyalty-mini">
                        <div className="loyalty-mini-text">Next {p.loyaltyProgress.nextTier}: {p.loyaltyProgress.remainingMiles} mi</div>
                        <div className="loyalty-mini-track"><div className="loyalty-mini-fill" style={{ width: `${p.loyaltyProgress.progressPct}%` }} /></div>
                      </div>
                    ) : <span className="badge badge-green">Top Tier</span>}
                  </td>
                  <td style={{ color: 'var(--gold)' }}>{p.milesAccount?.flightMiles ?? 0} mi</td>
                  <td>
                    <RoleGate
                      allowedRoles={['ADMIN']}
                      fallback={<span className="table-role-hint">Admin only</span>}
                    >
                      <div className="action-buttons">
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(p)}><Pencil size={13} /></button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p.id)}><Trash2 size={13} /></button>
                      </div>
                    </RoleGate>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <PaginationControls
          page={page}
          totalPages={totalPages}
          pageSize={pageSize}
          totalItems={totalItems}
          rangeStart={rangeStart}
          rangeEnd={rangeEnd}
          onPageChange={setPage}
          onPageSizeChange={(size) => { setPageSize(size); setPage(1); }}
        />
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
                  <label className="form-label">Auto Loyalty Status</label>
                  <input className="form-input" value={getStatusForMiles(form.milesAccount?.flightMiles || 0)} readOnly />
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
                <div className="loyalty-automation-note">
                  Next tier: {automationPreview.nextTier || 'Max tier reached'}
                  {automationPreview.nextTier ? ` • ${automationPreview.remainingMiles} miles remaining` : ''}
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