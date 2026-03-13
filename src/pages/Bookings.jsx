import { useMemo, useRef, useState } from 'react';
import Layout from '../components/Layout';
import { BookOpen, Plus, Trash2, X, RotateCcw, Pencil, CopyPlus, FileSpreadsheet, FileDown, Download } from 'lucide-react';
import { bookingService } from '../services/bookingService';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { usePagination } from '../hooks/usePagination';
import PaginationControls from '../components/PaginationControls';
import { exportTablePdf, exportToCsv, exportToExcel } from '../utils/exportUtils';
import { withOpsAndCapacity } from '../utils/flightOpsStore';
import { useBookingsQuery, useFlightsQuery, usePassengersQuery } from '../hooks/queries';
import { useFilterPresets } from '../hooks/useFilterPresets';
import { usePageShortcuts } from '../hooks/usePageShortcuts';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';
import { getErrorMessage } from '../utils/errorUtils';
import { bookingFormSchema } from '../validation/schemas';

const emptyForm = { kind: 'One-way', date: '', type: 'ECONOMIC', passengerId: '', flightId: '' };
const defaultFilters = {
  q: '',
  passengerId: 'all',
  flightId: 'all',
  type: 'all',
  kind: 'all',
  fromDate: '',
  toDate: '',
  sortBy: 'date-desc'
};

export default function Bookings() {
  const { data: bookings = [], isLoading: loadingBookings, refetch } = useBookingsQuery();
  const { data: passengers = [], isLoading: loadingPassengers } = usePassengersQuery();
  const { data: flights = [], isLoading: loadingFlights } = useFlightsQuery();
  const loading = loadingBookings || loadingPassengers || loadingFlights;
  const [showModal, setShowModal] = useState(false);
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
  } = useFilterPresets('bookings', defaultFilters);

  const {
    register,
    handleSubmit: handleFormSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(bookingFormSchema),
    defaultValues: emptyForm,
  });

  usePageShortcuts({
    onCreate: () => { setEditId(null); reset(emptyForm); setShowModal(true); },
    onSearch: () => searchInputRef.current?.focus(),
    onCloseModal: () => setShowModal(false),
    modalOpen: showModal,
  });

  const getPassenger = (id) => passengers.find(p => p.id === id);
  const flightsWithOps = useMemo(() => withOpsAndCapacity(flights, bookings), [flights, bookings]);
  const getFlight = (id) => flightsWithOps.find(f => f.id === id);

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
  } = usePagination(filteredBookings, 10);

  const exportRows = filteredBookings.map((b) => ({
    ID: b.id,
    Passenger: getPassenger(b.passengerId)?.name || `#${b.passengerId}`,
    Flight: b.flightId,
    Type: b.type,
    Kind: b.kind,
    Date: b.date,
  }));

  const handleSubmit = async (form) => {

    const selectedFlight = getFlight(parseInt(form.flightId));
    if (!selectedFlight) return toast.error('Selected flight not found');

    const blockedStatuses = ['Departed', 'Landed', 'Cancelled'];
    if (blockedStatuses.includes(selectedFlight.status)) {
      return toast.error(`Cannot book this flight: status is ${selectedFlight.status}`);
    }

    if ((selectedFlight.availableSeats || 0) <= 0 && !editId) {
      return toast.error('No seats available on this flight');
    }

    const today = new Date();
    const selectedDate = new Date(form.date);
    today.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);

    if (selectedDate < today) {
      return toast.error('Booking date cannot be in the past');
    }

    const sameDayConflict = bookings.some((b) =>
      b.passengerId === parseInt(form.passengerId) &&
      b.date === form.date &&
      b.id !== editId
    );

    if (sameDayConflict) {
      return toast.error('Passenger already has a booking on the same day');
    }

    try {
      const payload = { ...form, passengerId: parseInt(form.passengerId), flightId: parseInt(form.flightId) };
      if (editId) {
        await bookingService.update(editId, payload);
        toast.success('Booking updated!');
      } else {
        await bookingService.create(payload);
        toast.success('Booking created!');
      }
      setShowModal(false); refetch();
      setEditId(null);
      reset(emptyForm);
    } catch (error) { toast.error(getErrorMessage(error, 'Failed')); }
  };

  const openEdit = (b) => {
    setEditId(b.id);
    reset({
      kind: b.kind,
      date: b.date,
      type: b.type,
      passengerId: String(b.passengerId),
      flightId: String(b.flightId),
    });
    setShowModal(true);
  };

  const handleRebook = (b) => {
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    const nextDate = nextWeek.toISOString().slice(0, 10);

    setEditId(null);
    reset({
      kind: b.kind,
      type: b.type,
      passengerId: String(b.passengerId),
      flightId: String(b.flightId),
      date: nextDate,
    });
    setShowModal(true);
    toast.success('Rebook template loaded (date +7 days)');
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this booking?')) return;
    try { await bookingService.delete(id); toast.success('Deleted'); refetch(); }
    catch (error) { toast.error(getErrorMessage(error, 'Delete failed')); }
  };

  return (
    <Layout title="Bookings" subtitle="Manage all flight reservations">
      <div className="page-header">
        <div>
          <h1 className="page-title">Bookings</h1>
          <p className="page-subtitle">{bookings.length} reservation(s) total</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditId(null); reset(emptyForm); setShowModal(true); }}>
          <Plus size={16} /> New Booking
        </button>
      </div>

      <div className="table-container">
        <div className="table-header">
          <span className="table-title">All Reservations</span>
          <div className="table-tools">
            <button className="btn btn-secondary btn-sm" onClick={() => exportToCsv('bookings', exportRows)}><Download size={13} /> CSV</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportToExcel('bookings', exportRows, 'Bookings')}><FileSpreadsheet size={13} /> Excel</button>
            <button className="btn btn-secondary btn-sm" onClick={() => exportTablePdf('bookings', 'Bookings Report', exportRows)}><FileDown size={13} /> PDF</button>
          </div>
        </div>
        <div className="filters-wrap">
          <div className="filters-grid">
            <input
              ref={searchInputRef}
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
        {loading ? <TableSkeleton rows={10} cols={7} /> : (
          <table>
            <thead>
              <tr><th>ID</th><th>Passenger</th><th>Flight</th><th>Type</th><th>Kind</th><th>Date</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {filteredBookings.length === 0 ? (
                <tr><td colSpan={7}>
                  <EmptyState
                    icon={<BookOpen size={32} />}
                    title={bookings.length === 0 ? 'No bookings yet' : 'No booking matches your filters'}
                    description={bookings.length === 0 ? 'Create your first reservation to begin tracking demand.' : 'Try resetting filters or create a new booking.'}
                    ctaLabel={bookings.length === 0 ? 'Create Booking' : 'Reset Filters'}
                    onCta={bookings.length === 0 ? () => { setEditId(null); reset(emptyForm); setShowModal(true); } : resetFilters}
                  />
                </td></tr>
              ) : paginatedItems.map(b => {
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
                      <div className="action-buttons">
                        <button className="btn btn-secondary btn-sm" onClick={() => openEdit(b)}><Pencil size={13} /></button>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleRebook(b)}><CopyPlus size={13} /></button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}><Trash2 size={13} /></button>
                      </div>
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
              <span className="modal-title">{editId ? `Edit Booking #${editId}` : 'New Booking'}</span>
              <button className="modal-close" onClick={() => setShowModal(false)}><X size={16} /></button>
            </div>
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Passenger</label>
                <select className="form-select" {...register('passengerId')}>
                  <option value="">Select passenger</option>
                  {passengers.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
                {errors.passengerId && <small style={{ color: 'var(--danger)' }}>{errors.passengerId.message}</small>}
              </div>
              <div className="form-group">
                <label className="form-label">Flight</label>
                <select className="form-select" {...register('flightId')}>
                  <option value="">Select flight</option>
                  {flights.map(f => <option key={f.id} value={f.id}>Flight #{f.id} — {f.miles} miles</option>)}
                </select>
                {errors.flightId && <small style={{ color: 'var(--danger)' }}>{errors.flightId.message}</small>}
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Type</label>
                  <select className="form-select" {...register('type')}>
                    <option value="ECONOMIC">ECONOMIC</option>
                    <option value="BUSINESS">BUSINESS</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Kind</label>
                  <select className="form-select" {...register('kind')}>
                    <option>One-way</option>
                    <option>Round-trip</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Date</label>
                <input className="form-input" type="date" {...register('date')} />
                {errors.date && <small style={{ color: 'var(--danger)' }}>{errors.date.message}</small>}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleFormSubmit(handleSubmit)}>{editId ? 'Save Changes' : 'Create Booking'}</button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}