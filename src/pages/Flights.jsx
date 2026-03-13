import { useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../components/Layout';
import RoleGate from '../components/RoleGate';
import { Plane, Plus, Pencil, Trash2, X, RotateCcw, FileSpreadsheet, FileDown, Download } from 'lucide-react';
import { flightService } from '../services/flightService';
import toast from 'react-hot-toast';
import { usePagination } from '../hooks/usePagination';
import PaginationControls from '../components/PaginationControls';
import { exportTablePdf, exportToCsv, exportToExcel } from '../utils/exportUtils';
import { FLIGHT_STATUSES, saveFlightOps, withOpsAndCapacity, statusOrder } from '../utils/flightOpsStore';
import { useAirportsQuery, useBookingsQuery, useFlightsQuery } from '../hooks/queries';
import { useFilterPresets } from '../hooks/useFilterPresets';
import { usePageShortcuts } from '../hooks/usePageShortcuts';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';
import { getErrorMessage } from '../utils/errorUtils';
import { useAuth } from '../contexts/AuthContext';

const emptyForm = {
  time: '',
  miles: '',
  departureAirportId: '',
  arrivalAirportId: '',
  status: 'Scheduled',
  seatCapacity: 180,
  delayMinutes: 0,
};

const defaultFilters = {
  q: '',
  status: 'all',
  departureAirportId: 'all',
  arrivalAirportId: 'all',
  minMiles: '',
  maxMiles: '',
  minOccupancy: '',
  startTime: '',
  endTime: '',
  minHandlings: '',
  sortBy: 'id-desc'
};

export default function Flights() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const { data: flights = [], isLoading: loadingFlights, refetch } = useFlightsQuery();
  const { data: airports = [], isLoading: loadingAirports } = useAirportsQuery();
  const { data: bookings = [], isLoading: loadingBookings } = useBookingsQuery();
  const loading = loadingFlights || loadingAirports || loadingBookings;
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
  } = useFilterPresets('flights', defaultFilters);

  const getAirport = (id) => airports.find(a => a.id === id);
  const requireAdmin = () => {
    if (isAdmin) {
      return true;
    }
    toast.error('Admin access required for flight changes');
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
  const openEdit = (f) => {
    if (!requireAdmin()) {
      return;
    }
    setForm({
      time: f.time,
      miles: f.miles,
      departureAirportId: f.departureAirportId,
      arrivalAirportId: f.arrivalAirportId,
      status: f.status || 'Scheduled',
      seatCapacity: f.seatCapacity || 180,
      delayMinutes: f.delayMinutes || 0,
    });
    setEditId(f.id); setShowModal(true);
  };

  const flightsWithOps = useMemo(() => withOpsAndCapacity(flights, bookings), [flights, bookings]);

  const handleSubmit = async () => {
    if (!requireAdmin()) {
      return;
    }
    if (!form.departureAirportId || !form.arrivalAirportId) return toast.error('Select both airports');
    try {
      const payload = {
        time: form.time,
        miles: parseInt(form.miles) || 0,
        departureAirportId: parseInt(form.departureAirportId),
        arrivalAirportId: parseInt(form.arrivalAirportId),
      };
      let flightId = editId;
      if (editId) {
        await flightService.update(editId, payload);
        toast.success('Updated!');
      } else {
        const created = await flightService.create(payload);
        flightId = created.data?.id;
        toast.success('Flight created!');
      }

      if (flightId) {
        saveFlightOps(flightId, {
          status: form.status,
          seatCapacity: Number(form.seatCapacity || 180),
          delayMinutes: form.status === 'Delayed' ? Number(form.delayMinutes || 0) : 0,
        });
      }
      setShowModal(false); refetch();
    } catch (error) { toast.error(getErrorMessage(error, 'Failed')); }
  };

  const handleDelete = async (id) => {
    if (!requireAdmin()) {
      return;
    }
    if (!confirm('Delete this flight?')) return;
    try { await flightService.delete(id); toast.success('Deleted'); refetch(); }
    catch (error) { toast.error(getErrorMessage(error, 'Delete failed')); }
  };

  const filteredFlights = useMemo(() => {
    const q = filters.q.trim().toLowerCase();
    const minMiles = filters.minMiles === '' ? null : Number(filters.minMiles);
    const maxMiles = filters.maxMiles === '' ? null : Number(filters.maxMiles);
    const minOccupancy = filters.minOccupancy === '' ? null : Number(filters.minOccupancy);
    const minHandlings = filters.minHandlings === '' ? null : Number(filters.minHandlings);

    let rows = flightsWithOps.filter((f) => {
      const dep = airports.find((a) => a.id === f.departureAirportId);
      const arr = airports.find((a) => a.id === f.arrivalAirportId);

      const textOk = !q || [
        String(f.id || ''),
        dep?.name,
        dep?.shortName,
        arr?.name,
        arr?.shortName
      ].some((v) => (v || '').toLowerCase().includes(q));

      const statusOk = filters.status === 'all' || f.status === filters.status;
      const depOk = filters.departureAirportId === 'all' || String(f.departureAirportId || '') === filters.departureAirportId;
      const arrOk = filters.arrivalAirportId === 'all' || String(f.arrivalAirportId || '') === filters.arrivalAirportId;

      const miles = Number(f.miles || 0);
      const minMilesOk = minMiles === null || miles >= minMiles;
      const maxMilesOk = maxMiles === null || miles <= maxMiles;
      const occupancyOk = minOccupancy === null || Number(f.occupancyPct || 0) >= minOccupancy;

      const flightTime = f.time || '';
      const startOk = !filters.startTime || flightTime >= filters.startTime;
      const endOk = !filters.endTime || flightTime <= filters.endTime;

      const handlingCount = Number(f.flightHandlings?.length || 0);
      const handlingsOk = minHandlings === null || handlingCount >= minHandlings;

      return textOk && statusOk && depOk && arrOk && minMilesOk && maxMilesOk && occupancyOk && startOk && endOk && handlingsOk;
    });

    rows = [...rows].sort((a, b) => {
      if (filters.sortBy === 'status') return statusOrder(a.status) - statusOrder(b.status);
      if (filters.sortBy === 'occupancy-desc') return Number(b.occupancyPct || 0) - Number(a.occupancyPct || 0);
      if (filters.sortBy === 'miles-asc') return Number(a.miles || 0) - Number(b.miles || 0);
      if (filters.sortBy === 'miles-desc') return Number(b.miles || 0) - Number(a.miles || 0);
      if (filters.sortBy === 'time-asc') return (a.time || '').localeCompare(b.time || '');
      if (filters.sortBy === 'time-desc') return (b.time || '').localeCompare(a.time || '');
      return Number(b.id || 0) - Number(a.id || 0);
    });

    return rows;
  }, [flightsWithOps, airports, filters]);

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
  } = usePagination(filteredFlights, 10);

  const exportRows = filteredFlights.map((f) => ({
    ID: f.id,
    Time: f.time,
    Status: f.status,
    From: getAirport(f.departureAirportId)?.shortName || f.departureAirportId,
    To: getAirport(f.arrivalAirportId)?.shortName || f.arrivalAirportId,
    Miles: f.miles,
    SeatCapacity: f.seatCapacity,
    AvailableSeats: f.availableSeats,
    OccupancyPct: `${f.occupancyPct}%`,
    Handlings: f.flightHandlings?.length || 0,
  }));

  return (
    <Layout title="Flights" subtitle="Manage all flight routes">
      <div className="page-header">
        <div>
          <h1 className="page-title">Flights</h1>
          <p className="page-subtitle">{flights.length} flight(s) scheduled</p>
        </div>
        <RoleGate
          allowedRoles={['ADMIN']}
          fallback={<span className="page-role-note">Admin role required to create or edit flights.</span>}
        >
          <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Flight</button>
        </RoleGate>
      </div>

      <div className="table-container">
        <div className="table-header">
          <span className="table-title">All Flights</span>
          <div className="table-tools">
            <button className="btn btn-secondary btn-sm" onClick={() => exportToCsv('flights', exportRows)}><Download size={13} /> CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportToExcel('flights', exportRows, 'Flights')}><FileSpreadsheet size={13} /> Excel</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportTablePdf('flights', 'Flights Report', exportRows)}><FileDown size={13} /> PDF</button>
          </div>
        </div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              ref={searchInputRef}
              className="filter-input"
              placeholder="Search by ID or airport..."
              value={filters.q}
              onChange={(e) => setFilters({ ...filters, q: e.target.value })}
            />
            <select
              className="filter-select"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <option value="all">Status: All</option>
              {FLIGHT_STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
            </select>
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
              type="number"
              placeholder="Min occupancy %"
              value={filters.minOccupancy}
              onChange={(e) => setFilters({ ...filters, minOccupancy: e.target.value })}
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
              <option value="status">Sort: Status lifecycle</option>
              <option value="occupancy-desc">Sort: Occupancy high-low</option>
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
        {loading ? <TableSkeleton rows={10} cols={10} /> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Status</th><th>Time</th><th>From</th><th>To</th><th>Miles</th><th>Seats</th><th>Occupancy</th><th>Handlings</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredFlights.length === 0 ? (
                <tr><td colSpan={10}>
                  <EmptyState
                    icon={<Plane size={32} />}
                    title={flights.length === 0 ? 'No flights yet' : 'No flight matches your filters'}
                    description={flights.length === 0 ? 'Create your first route to begin operations.' : 'Try resetting filters or add a new flight.'}
                    ctaLabel={flights.length === 0 ? (isAdmin ? 'Create Flight' : null) : 'Reset Filters'}
                    onCta={flights.length === 0 ? (isAdmin ? openCreate : null) : resetFilters}
                  />
                </td></tr>
              ) : paginatedItems.map(f => {
                const dep = getAirport(f.departureAirportId);
                const arr = getAirport(f.arrivalAirportId);
                return (
                  <tr key={f.id}>
                    <td><Link to={`/flights/${f.id}`} className="table-link">#{f.id}</Link></td>
                    <td>
                      <span className={`badge ${
                        f.status === 'Cancelled' ? 'badge-red' :
                        f.status === 'Delayed' ? 'badge-red' :
                        f.status === 'Landed' ? 'badge-green' :
                        'badge-blue'
                      }`}>{f.status}</span>
                    </td>
                    <td style={{ fontFamily: 'monospace' }}>{f.time}</td>
                    <td>
                      {dep ? <span className="badge badge-blue">{dep.shortName}</span> : `#${f.departureAirportId}`}
                    </td>
                    <td>
                      {arr ? <span className="badge badge-gold">{arr.shortName}</span> : `#${f.arrivalAirportId}`}
                    </td>
                    <td style={{ color: 'var(--gold)' }}>{f.miles?.toLocaleString()} mi</td>
                    <td>{f.availableSeats}/{f.seatCapacity}</td>
                    <td>
                      <span className={`badge ${f.occupancyPct >= 90 ? 'badge-red' : f.occupancyPct >= 70 ? 'badge-gold' : 'badge-green'}`}>
                        {f.occupancyPct}%
                      </span>
                    </td>
                    <td><span className="badge badge-green">{f.flightHandlings?.length ?? 0}</span></td>
                    <td>
                      <RoleGate
                        allowedRoles={['ADMIN']}
                        fallback={<span className="table-role-hint">Admin only</span>}
                      >
                        <div className="action-buttons">
                          <button className="btn btn-secondary btn-sm" onClick={() => openEdit(f)}><Pencil size={13} /></button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(f.id)}><Trash2 size={13} /></button>
                        </div>
                      </RoleGate>
                    </td>
                  </tr>
                );
              })}
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
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Lifecycle Status</label>
                  <select className="form-select" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                    {FLIGHT_STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Seat Capacity</label>
                  <input className="form-input" type="number" value={form.seatCapacity} onChange={e => setForm({ ...form, seatCapacity: e.target.value })} placeholder="180" />
                </div>
              </div>
              {form.status === 'Delayed' && (
                <div className="form-group">
                  <label className="form-label">Delay (minutes)</label>
                  <input className="form-input" type="number" value={form.delayMinutes} onChange={e => setForm({ ...form, delayMinutes: e.target.value })} placeholder="45" />
                </div>
              )}
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