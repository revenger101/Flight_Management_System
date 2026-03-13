import { useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { ArrowLeft, Plane, Users, Gauge } from 'lucide-react';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts';
import { FLIGHT_STATUSES, withOpsAndCapacity } from '../utils/flightOpsStore';
import { useFlightDetailsQuery } from '../hooks/queries';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';

export default function FlightDetails() {
  const { id } = useParams();
  const { data, isLoading: loading } = useFlightDetailsQuery(id);
  const flight = data?.flight || null;
  const airports = data?.airports || [];
  const bookings = data?.bookings || [];

  const depAirport = useMemo(() => airports.find((a) => a.id === flight?.departureAirportId), [airports, flight]);
  const arrAirport = useMemo(() => airports.find((a) => a.id === flight?.arrivalAirportId), [airports, flight]);
  const flightWithOps = useMemo(() => {
    if (!flight) return null;
    return withOpsAndCapacity([flight], bookings)[0];
  }, [flight, bookings]);

  const classMixData = useMemo(() => {
    const business = bookings.filter((b) => b.type === 'BUSINESS').length;
    const economic = bookings.filter((b) => b.type === 'ECONOMIC').length;
    return [
      { name: 'Business', value: business },
      { name: 'Economic', value: economic },
    ];
  }, [bookings]);

  const monthlyPerformance = useMemo(() => {
    const now = new Date();
    const buckets = [];
    for (let i = 5; i >= 0; i -= 1) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      buckets.push({
        key,
        month: d.toLocaleDateString(undefined, { month: 'short' }),
        bookings: 0,
      });
    }

    bookings.forEach((b) => {
      const key = (b.date || '').slice(0, 7);
      const bucket = buckets.find((x) => x.key === key);
      if (bucket) bucket.bookings += 1;
    });

    return buckets;
  }, [bookings]);

  if (loading) {
    return (
      <Layout title="Flight Details" subtitle={`Flight #${id}`}>
        <TableSkeleton rows={8} cols={5} />
      </Layout>
    );
  }

  if (!flight) {
    return (
      <Layout title="Flight Details" subtitle={`Flight #${id}`}>
        <EmptyState
          icon={<Plane size={32} />}
          title="Flight not found"
          description="The requested flight does not exist or was removed."
          ctaLabel="Back to Flights"
          onCta={() => window.history.back()}
        />
      </Layout>
    );
  }

  return (
    <Layout title="Flight Details" subtitle={`Flight #${id} performance and bookings`}>
      <div className="page-header">
        <div>
          <h1 className="page-title">Flight #{flight.id}</h1>
          <p className="page-subtitle">
            {depAirport?.shortName || 'N/A'} {'->'} {arrAirport?.shortName || 'N/A'}
          </p>
        </div>
        <Link className="btn btn-secondary" to="/flights"><ArrowLeft size={15} /> Back to Flights</Link>
      </div>

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'var(--blue-dim)', color: 'var(--blue-accent)' }}><Plane size={18} /></div><div className="stat-value">{flight.miles || 0}</div><div className="stat-label">Miles</div></div>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'rgba(34,197,94,0.1)', color: 'var(--success)' }}><Users size={18} /></div><div className="stat-value">{bookings.length}</div><div className="stat-label">Bookings Count</div></div>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'var(--gold-dim)', color: 'var(--gold)' }}><Gauge size={18} /></div><div className="stat-value">{flightWithOps?.occupancyPct || 0}%</div><div className="stat-label">Occupancy</div></div>
      </div>

      <div className="analytics-card" style={{ marginBottom: 16 }}>
        <div className="analytics-card-header">
          <span className="analytics-title">Lifecycle Timeline</span>
          <span className={`badge ${flightWithOps?.status === 'Cancelled' || flightWithOps?.status === 'Delayed' ? 'badge-red' : 'badge-blue'}`}>
            {flightWithOps?.status || 'Scheduled'}
          </span>
        </div>
        <div className="flight-timeline">
          {FLIGHT_STATUSES.map((status, index) => {
            const activeIndex = FLIGHT_STATUSES.indexOf(flightWithOps?.status || 'Scheduled');
            const isDone = index <= activeIndex;
            return (
              <div key={status} className={`timeline-step ${isDone ? 'is-done' : ''}`}>
                <span className="timeline-dot" />
                <span>{status}</span>
              </div>
            );
          })}
        </div>
        <div className="filters-meta" style={{ marginTop: 10 }}>
          <span className="filter-chip">Seats: <strong>{flightWithOps?.availableSeats || 0}/{flightWithOps?.seatCapacity || 0}</strong></span>
          {flightWithOps?.status === 'Delayed' && <span className="filter-chip">Delay: <strong>{flightWithOps.delayMinutes} min</strong></span>}
        </div>
      </div>

      <div className="dashboard-analytics-grid" style={{ marginTop: 0 }}>
        <div className="analytics-card">
          <div className="analytics-card-header">
            <span className="analytics-title">Class Mix</span>
            <span className="analytics-sub">Flight Bookings</span>
          </div>
          <div className="chart-shell">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={classMixData} dataKey="value" nameKey="name" innerRadius={52} outerRadius={82}>
                  <Cell fill="#c9a84c" />
                  <Cell fill="#3b82f6" />
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="analytics-card">
          <div className="analytics-card-header">
            <span className="analytics-title">Route Performance</span>
            <span className="analytics-sub">Last 6 months</span>
          </div>
          <div className="chart-shell">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthlyPerformance}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                <XAxis dataKey="month" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="bookings" fill="#60a5fa" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">Related Bookings</span></div>
        <table>
          <thead>
            <tr><th>ID</th><th>Passenger ID</th><th>Type</th><th>Kind</th><th>Date</th></tr>
          </thead>
          <tbody>
            {bookings.length === 0 ? (
              <tr><td colSpan={5}><div className="table-empty">No bookings for this flight.</div></td></tr>
            ) : bookings.map((b) => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td>#{b.passengerId}</td>
                <td><span className={`badge ${b.type === 'BUSINESS' ? 'badge-gold' : 'badge-blue'}`}>{b.type}</span></td>
                <td>{b.kind}</td>
                <td>{b.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
