import { useMemo, useRef, useState } from 'react';
import Layout from '../components/Layout';
import { Building2, Plus, Pencil, Trash2, X, RotateCcw, FileSpreadsheet, FileDown, Download } from 'lucide-react';
import { airportService } from '../services/airportService';
import toast from 'react-hot-toast';
import { usePagination } from '../hooks/usePagination';
import PaginationControls from '../components/PaginationControls';
import { exportTablePdf, exportToCsv, exportToExcel } from '../utils/exportUtils';
import { useAirlinesQuery, useAirportsQuery } from '../hooks/queries';
import { useFilterPresets } from '../hooks/useFilterPresets';
import { usePageShortcuts } from '../hooks/usePageShortcuts';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';
import { getErrorMessage } from '../utils/errorUtils';

const emptyForm = { name: '', shortName: '', country: '', fee: '', airlineId: '' };
const defaultFilters = {
  q: '',
  country: '',
  airlineId: 'all',
  minFee: '',
  maxFee: '',
  sortBy: 'name-asc'
};

export default function Airports() {
  const { data: airports = [], isLoading: loadingAirports, refetch } = useAirportsQuery();
  const { data: airlines = [], isLoading: loadingAirlines } = useAirlinesQuery();
  const loading = loadingAirports || loadingAirlines;
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
  } = useFilterPresets('airports', defaultFilters);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };

  usePageShortcuts({
    onCreate: openCreate,
    onSearch: () => searchInputRef.current?.focus(),
    onCloseModal: () => setShowModal(false),
    modalOpen: showModal,
  });

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
      setShowModal(false); refetch();
    } catch (error) { toast.error(getErrorMessage(error, 'Operation failed')); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this airport?')) return;
    try { await airportService.delete(id); toast.success('Deleted'); refetch(); }
    catch (error) { toast.error(getErrorMessage(error, 'Delete failed')); }
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
  } = usePagination(filteredAirports, 10);

  const exportRows = filteredAirports.map((a) => ({
    ID: a.id,
    Name: a.name,
    Code: a.shortName,
    Country: a.country,
    Fee: a.fee,
    Airline: getAirlineName(a.airlineId),
  }));

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
        <div className="table-header">
          <span className="table-title">All Airports</span>
          <div className="table-tools">
            <button className="btn btn-secondary btn-sm" onClick={() => exportToCsv('airports', exportRows)}><Download size={13} /> CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportToExcel('airports', exportRows, 'Airports')}><FileSpreadsheet size={13} /> Excel</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportTablePdf('airports', 'Airports Report', exportRows)}><FileDown size={13} /> PDF</button>
          </div>
        </div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              ref={searchInputRef}
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
        {loading ? <TableSkeleton rows={8} cols={7} /> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Name</th><th>Code</th><th>Country</th><th>Fee</th><th>Airline</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredAirports.length === 0 ? (
                <tr><td colSpan={7}>
                  <EmptyState
                    icon={<Building2 size={32} />}
                    title={airports.length === 0 ? 'No airports yet' : 'No airport matches your filters'}
                    description={airports.length === 0 ? 'Create your first airport to start adding routes.' : 'Try changing filters or add a new airport.'}
                    ctaLabel={airports.length === 0 ? 'Create Airport' : 'Reset Filters'}
                    onCta={airports.length === 0 ? openCreate : resetFilters}
                  />
                </td></tr>
              ) : paginatedItems.map(a => (
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