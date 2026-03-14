import { useMemo, useState } from 'react';
import { ScanLine, RefreshCw, Search, Armchair, Ticket, RotateCcw, CircleDollarSign, BellRing, CheckCircle, Clock3 } from 'lucide-react';
import Layout from '../components/Layout';
import { passengerPortalService } from '../services/passengerPortalService';

function fmtDate(v) {
  if (!v) return '—';
  return new Date(v).toLocaleString([], { month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

export default function PassengerPortal() {
  const [passengerId, setPassengerId] = useState('');
  const [bookings, setBookings] = useState([]);
  const [requests, setRequests] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [boardingPass, setBoardingPass] = useState(null);
  const [seatInput, setSeatInput] = useState('');
  const [reasonInput, setReasonInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const load = async () => {
    if (!passengerId) return;
    setLoading(true);
    setError(null);
    try {
      const [bRes, rRes] = await Promise.all([
        passengerPortalService.getBookingsByPassenger(passengerId),
        passengerPortalService.getRequestsByPassenger(passengerId),
      ]);
      setBookings(bRes.data || []);
      setRequests(rRes.data || []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load passenger portal data');
    } finally {
      setLoading(false);
    }
  };

  const statusCount = useMemo(() => ({
    active: bookings.filter((b) => b.status === 'CONFIRMED' || b.status === 'REBOOKED').length,
    checkedIn: bookings.filter((b) => b.checkedIn).length,
    pendingReq: requests.filter((r) => r.status === 'SUBMITTED' || r.status === 'UNDER_REVIEW').length,
  }), [bookings, requests]);

  const onSelectSeat = async (booking) => {
    if (!seatInput) return;
    try {
      await passengerPortalService.selectSeat(booking.bookingId, seatInput);
      setSeatInput('');
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Seat update failed');
    }
  };

  const onCheckIn = async (booking) => {
    try {
      const { data } = await passengerPortalService.checkIn(booking.bookingId);
      setBoardingPass(data);
      setSelectedBooking(booking.bookingId);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Check-in failed');
    }
  };

  const submitRequest = async (bookingId, kind) => {
    try {
      if (kind === 'refund') {
        await passengerPortalService.requestRefund(bookingId, reasonInput || undefined);
      } else {
        await passengerPortalService.requestRebook(bookingId, reasonInput || undefined);
      }
      setReasonInput('');
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Request submission failed');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Passenger Self-Service Portal</h1>
          <p className="page-subtitle">Manage bookings, check in, boarding pass, seat changes, and service workflows</p>
        </div>
      </div>

      <div className="portal-hero">
        <div className="portal-hero-copy">
          <h2>Travel Control Hub</h2>
          <p>Give passengers direct control and reduce support load with transparent, trackable workflows.</p>
        </div>
        <div className="portal-search">
          <input
            type="number"
            placeholder="Enter Passenger ID"
            value={passengerId}
            onChange={(e) => setPassengerId(e.target.value)}
          />
          <button className="btn btn-primary" onClick={load}>
            <Search size={14} /> Find
          </button>
        </div>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}

      <div className="portal-kpis">
        <div className="portal-kpi"><Ticket size={15} /><strong>{statusCount.active}</strong><span>Active Bookings</span></div>
        <div className="portal-kpi"><CheckCircle size={15} /><strong>{statusCount.checkedIn}</strong><span>Checked In</span></div>
        <div className="portal-kpi"><Clock3 size={15} /><strong>{statusCount.pendingReq}</strong><span>Open Requests</span></div>
      </div>

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading portal…</div>}

      <div className="portal-grid">
        <section className="ops-section">
          <div className="ops-section-header">
            <h2><Ticket size={16} /> Bookings</h2>
            <span className="ops-count">{bookings.length}</span>
          </div>
          <div className="portal-booking-list">
            {bookings.map((b) => (
              <article key={b.bookingId} className="portal-booking-card">
                <header>
                  <strong>#{b.bookingId} {b.route}</strong>
                  <span className={`status-badge ${b.status === 'CANCELLED' ? 'status-cancelled' : b.status === 'WAITLISTED' ? 'status-delayed' : 'status-landed'}`}>
                    {b.status}
                  </span>
                </header>
                <div className="portal-booking-meta">
                  <span>Class: {b.type}</span>
                  <span>Date: {b.date}</span>
                  <span>Seat: {b.seatNumber || 'Not selected'}</span>
                  <span>Fare: {b.finalFare || 0} {b.currency || 'USD'}</span>
                </div>
                <div className="portal-booking-actions">
                  <div className="inline-input-action">
                    <input
                      placeholder="Seat (e.g. 12A)"
                      value={selectedBooking === b.bookingId ? seatInput : ''}
                      onChange={(e) => {
                        setSelectedBooking(b.bookingId);
                        setSeatInput(e.target.value.toUpperCase());
                      }}
                    />
                    <button className="btn btn-secondary btn-sm" onClick={() => onSelectSeat(b)}>
                      <Armchair size={13} /> Set Seat
                    </button>
                  </div>
                  <button className="btn btn-success btn-sm" onClick={() => onCheckIn(b)}>
                    <ScanLine size={13} /> Check-In
                  </button>
                  <button className="btn btn-secondary btn-sm" onClick={() => submitRequest(b.bookingId, 'rebook')}>
                    <RotateCcw size={13} /> Rebook Request
                  </button>
                  <button className="btn btn-secondary btn-sm" onClick={() => submitRequest(b.bookingId, 'refund')}>
                    <CircleDollarSign size={13} /> Refund Request
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="ops-section">
          <div className="ops-section-header">
            <h2><ScanLine size={16} /> Boarding Pass</h2>
          </div>
          {boardingPass ? (
            <div className="boarding-pass-card">
              <div className="boarding-pass-header">
                <span>Passenger</span>
                <strong>{boardingPass.passengerName}</strong>
              </div>
              <div className="boarding-pass-body">
                <div><span>Route</span><strong>{boardingPass.route}</strong></div>
                <div><span>Seat</span><strong>{boardingPass.seatNumber}</strong></div>
                <div><span>Gate</span><strong>{boardingPass.gate}</strong></div>
                <div><span>Departure</span><strong>{fmtDate(boardingPass.departure)}</strong></div>
              </div>
              <div className="boarding-pass-code">{boardingPass.boardingPassCode}</div>
            </div>
          ) : (
            <p className="text-muted">Check in a booking to generate a digital boarding pass.</p>
          )}

          <div className="portal-reason-box">
            <label>Request Notes</label>
            <textarea
              rows={4}
              placeholder="Explain reason for rebooking/refund"
              value={reasonInput}
              onChange={(e) => setReasonInput(e.target.value)}
            />
          </div>
        </section>
      </div>

      <section className="ops-section">
        <div className="ops-section-header">
          <h2><BellRing size={16} /> Workflow Tracking</h2>
          <span className="ops-count">{requests.length}</span>
        </div>
        <div className="portal-request-timeline">
          {requests.map((r) => (
            <div key={r.id} className="portal-request-item">
              <div className="timeline-dot" />
              <div className="timeline-content">
                <div className="timeline-title">
                  <strong>{r.type}</strong>
                  <span className={`status-badge ${r.status === 'COMPLETED' ? 'status-landed' : r.status === 'REJECTED' ? 'status-cancelled' : 'status-boarding'}`}>
                    {r.status}
                  </span>
                </div>
                <p>{r.reason}</p>
                <small>Booking #{r.bookingId} • Created {fmtDate(r.createdAt)}</small>
              </div>
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}
