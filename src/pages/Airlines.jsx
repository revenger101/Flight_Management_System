import { useMemo, useRef, useState } from 'react';
import Layout from '../components/Layout';
import StatCard from '../components/StatCard';
import { Briefcase, Plus, Pencil, Trash2, X, Image as ImageIcon, RotateCcw, FileSpreadsheet, FileDown, Download } from 'lucide-react';
import { airlineService } from '../services/airlineService';
import toast from 'react-hot-toast';
import { usePagination } from '../hooks/usePagination';
import PaginationControls from '../components/PaginationControls';
import { exportTablePdf, exportToCsv, exportToExcel } from '../utils/exportUtils';
import { useAirlinesQuery } from '../hooks/queries';
import { useFilterPresets } from '../hooks/useFilterPresets';
import { usePageShortcuts } from '../hooks/usePageShortcuts';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';
import { getErrorMessage } from '../utils/errorUtils';

const emptyForm = { name: '', shortName: '', logo: '' };
const defaultFilters = {
  q: '',
  short: '',
  hasLogo: 'all',
  sortBy: 'name-asc'
};

export default function Airlines() {
  const { data: airlines = [], isLoading: loading, refetch } = useAirlinesQuery();
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
    deletePreset,
  } = useFilterPresets('airlines', defaultFilters);

  const filteredAirlines = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    const short = filters.short.trim().toLowerCase();

    let rows = airlines.filter((a) => {
      const textOk = !q || [a.name, a.shortName].some((v) => (v || '').toLowerCase().includes(q));
      const shortOk = !short || (a.shortName || '').toLowerCase().includes(short);
      const logoOk = filters.hasLogo === 'all' ? true : filters.hasLogo === 'yes' ? !!a.logo : !a.logo;
      return textOk && shortOk && logoOk;
    });

    rows = [...rows].sort((a, b) => {
      if (filters.sortBy === 'name-desc') return (b.name || '').localeCompare(a.name || '');
      if (filters.sortBy === 'id-asc') return (a.id || 0) - (b.id || 0);
      if (filters.sortBy === 'id-desc') return (b.id || 0) - (a.id || 0);
      return (a.name || '').localeCompare(b.name || '');
    });

    return rows;
  }, [airlines, filters]);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setShowModal(true); };

  usePageShortcuts({
    onCreate: openCreate,
    onSearch: () => searchInputRef.current?.focus(),
    onCloseModal: () => setShowModal(false),
    modalOpen: showModal,
  });

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
  } = usePagination(filteredAirlines, 10);

  const exportRows = filteredAirlines.map((a) => ({
    ID: a.id,
    Name: a.name,
    ShortName: a.shortName,
    HasLogo: a.logo ? 'Yes' : 'No',
    LogoUrl: a.logo || '',
  }));

  const openEdit = (a) => { setForm({ name: a.name, shortName: a.shortName, logo: a.logo }); setEditId(a.id); setShowModal(true); };

  const handleSubmit = async () => {
    if (!form.name || !form.shortName) return toast.error('Name and Short Name are required');
    try {
      if (editId) {
        await airlineService.update(editId, form);
        toast.success('Airline updated!');
      } else {
        await airlineService.create(form);
        toast.success('Airline created!');
      }
      setShowModal(false);
      refetch();
    } catch (error) { toast.error(getErrorMessage(error, 'Operation failed')); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this airline?')) return;
    try {
      await airlineService.delete(id);
      toast.success('Airline deleted');
      refetch();
    } catch (error) { toast.error(getErrorMessage(error, 'Delete failed')); }
  };

  return (
    <Layout title="Airlines" subtitle="Manage all airline companies">
      <div className="page-header">
        <div>
          <h1 className="page-title">Airlines</h1>
          <p className="page-subtitle">{airlines.length} airline(s) registered</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>
          <Plus size={16} /> Add Airline
        </button>
      </div>

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)', maxWidth: 600 }}>
        <StatCard icon={<Briefcase size={18} />} value={airlines.length} label="Total Airlines" />
      </div>

      <div className="table-container">
        <div className="table-header">
          <span className="table-title">All Airlines</span>
          <div className="table-tools">
            <button className="btn btn-secondary btn-sm" onClick={() => exportToCsv('airlines', exportRows)}><Download size={13} /> CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportToExcel('airlines', exportRows, 'Airlines')}><FileSpreadsheet size={13} /> Excel</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportTablePdf('airlines', 'Airlines Report', exportRows)}><FileDown size={13} /> PDF</button>
          </div>
        </div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              ref={searchInputRef}
              className="filter-input"
              placeholder="Search by airline name or short name..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <input
              className="filter-input"
              placeholder="Short name contains..."
              value={filters.short}
              onChange={(e) => setFilters({ ...filters, short: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.hasLogo}
              onChange={(e) => setFilters({ ...filters, hasLogo: e.target.value })}
            >
              <option value="all">Logo: All</option>
              <option value="yes">With logo</option>
              <option value="no">Without logo</option>
            </select>
            <select
              className="filter-select"
              value={filters.sortBy}
              onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
            >
              <option value="name-asc">Sort: Name A-Z</option>
              <option value="name-desc">Sort: Name Z-A</option>
              <option value="id-asc">Sort: ID asc</option>
              <option value="id-desc">Sort: ID desc</option>
            </select>
            <button className="btn btn-secondary" onClick={resetFilters}>
              <RotateCcw size={14} /> Reset
            </button>
          </div>
          <div className="filters-meta">
            <span className="filter-chip">Showing <strong>{filteredAirlines.length}</strong> / {airlines.length}</span>
            {filters.q && <span className="filter-chip">Query: <strong>{filters.q}</strong></span>}
            {filters.hasLogo !== 'all' && <span className="filter-chip">Logo: <strong>{filters.hasLogo}</strong></span>}
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
            {presets.length > 0 && (
              <button className="btn btn-secondary btn-sm" onClick={() => deletePreset(presets[presets.length - 1].id)}>Delete Last Preset</button>
            )}
          </div>
        </div>
        {loading ? (
          <TableSkeleton rows={8} cols={5} />
        ) : (
          <table>
            <thead>
              <tr>
                <th>ID</th><th>Airline</th><th>Name</th><th>Short Name</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredAirlines.length === 0 ? (
                <tr><td colSpan={5}>
                  <EmptyState
                    icon={<Briefcase size={32} />}
                    title={airlines.length === 0 ? 'No airlines yet' : 'No airline matches your filters'}
                    description={airlines.length === 0 ? 'Add your first airline to start building your network.' : 'Reset filters or create a new airline.'}
                    ctaLabel={airlines.length === 0 ? 'Create Airline' : 'Reset Filters'}
                    onCta={airlines.length === 0 ? openCreate : resetFilters}
                  />
                </td></tr>
              ) : paginatedItems.map(a => (
                <tr key={a.id}>
                  <td>#{a.id}</td>
                  <td>
                    {a.logo ? (
                      <img 
                        src={a.logo} 
                        alt={a.name} 
                        style={{ width: '40px', height: '40px', objectFit: 'contain', borderRadius: '4px', background: '#f8f9fa' }}
                        onError={(e) => { e.target.src = "https://placehold.co/40x40?text=✈️"; }}
                      />
                    ) : (
                      <div style={{ width: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#eee', borderRadius: '4px' }}>
                        <ImageIcon size={16} color="#999" />
                      </div>
                    )}
                  </td>
                  <td>{a.name}</td>
                  <td><span className="badge badge-gold">{a.shortName}</span></td>
                  <td>
                    <div className="action-buttons">
                      <button className="btn btn-secondary btn-sm" onClick={() => openEdit(a)}>
                        <Pencil size={13} />
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a.id)}>
                        <Trash2 size={13} />
                      </button>
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
              <span className="modal-title">{editId ? 'Edit Airline' : 'New Airline'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Airline Name *</label>
                <input className="form-input" value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Air France" />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Short Name *</label>
                  <input className="form-input" value={form.shortName}
                    onChange={e => setForm({ ...form, shortName: e.target.value })} placeholder="e.g. AF" />
                </div>
                <div className="form-group">
                  <label className="form-label">Logo URL</label>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <input className="form-input" value={form.logo}
                      onChange={e => setForm({ ...form, logo: e.target.value })} placeholder="https://..." />
                    {form.logo && (
                       <img 
                        src={form.logo} 
                        alt="Preview" 
                        style={{ width: '42px', height: '42px', objectFit: 'contain', borderRadius: '4px', border: '1px solid #ddd' }}
                        onError={(e) => { e.target.style.display = 'none'; }}
                       />
                    )}
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSubmit}>
                {editId ? 'Save Changes' : 'Create Airline'}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}