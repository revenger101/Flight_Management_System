import { useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import Layout from '../components/Layout';
import { ArrowLeft, UserRound, BookOpen, TrendingUp } from 'lucide-react';
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, BarChart, Bar } from 'recharts';
import { usePassengerDetailsQuery } from '../hooks/queries';
import TableSkeleton from '../components/TableSkeleton';
import EmptyState from '../components/EmptyState';

export default function PassengerDetails() {
  const { id } = useParams();
  const { data, isLoading: loading } = usePassengerDetailsQuery(id);
  const passenger = data?.passenger || null;
  const bookings = data?.bookings || [];
  const flights = data?.flights || [];

  const flightById = useMemo(() => {
    const map = new Map();
    flights.forEach((f) => map.set(f.id, f));
    return map;
  }, [flights]);

  const milesTrend = useMemo(() => {
    const sorted = [...bookings].sort((a, b) => (a.date || '').localeCompare(b.date || ''));
    let cumulative = 0;
    return sorted.map((b, idx) => {
      cumulative += Number(flightById.get(b.flightId)?.miles || 0);
      return {
        point: idx + 1,
        date: b.date,
        cumulativeMiles: cumulative,
      };
    });
  }, [bookings, flightById]);

  const classBreakdown = useMemo(() => {
    const economic = bookings.filter((b) => b.type === 'ECONOMIC').length;
    const business = bookings.filter((b) => b.type === 'BUSINESS').length;
    return [
      { name: 'Economic', value: economic },
      { name: 'Business', value: business },
    ];
  }, [bookings]);

  if (loading) {
    return (
      <Layout title="Passenger Details" subtitle={`Passenger #${id}`}>
        <TableSkeleton rows={8} cols={6} />
      </Layout>
    );
  }

  if (!passenger) {
    return (
      <Layout title="Passenger Details" subtitle={`Passenger #${id}`}>
        <EmptyState
          icon={<UserRound size={32} />}
          title="Passenger not found"
          description="This passenger profile is unavailable or was removed."
          ctaLabel="Back to Passengers"
          onCta={() => window.history.back()}
        />
      </Layout>
    );
  }

  return (
    <Layout title="Passenger Details" subtitle={`Passenger #${id} travel profile`}>
      <div className="page-header">
        <div>
          <h1 className="page-title">{passenger.name}</h1>
          <p className="page-subtitle">{passenger.status} tier - {passenger.milesAccount?.flightMiles || 0} miles</p>
        </div>
        <Link className="btn btn-secondary" to="/passengers"><ArrowLeft size={15} /> Back to Passengers</Link>
      </div>

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'var(--blue-dim)', color: 'var(--blue-accent)' }}><UserRound size={18} /></div><div className="stat-value">{passenger.status || 'N/A'}</div><div className="stat-label">Current Status</div></div>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'rgba(34,197,94,0.1)', color: 'var(--success)' }}><BookOpen size={18} /></div><div className="stat-value">{bookings.length}</div><div className="stat-label">Total Bookings</div></div>
        <div className="stat-card"><div className="stat-icon" style={{ background: 'var(--gold-dim)', color: 'var(--gold)' }}><TrendingUp size={18} /></div><div className="stat-value">{passenger.milesAccount?.flightMiles || 0}</div><div className="stat-label">Flight Miles</div></div>
      </div>

      <div className="dashboard-analytics-grid" style={{ marginTop: 0 }}>
        <div className="analytics-card">
          <div className="analytics-card-header">
            <span className="analytics-title">Miles Trend</span>
            <span className="analytics-sub">Cumulative</span>
          </div>
          <div className="chart-shell">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={milesTrend}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                <XAxis dataKey="point" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="cumulativeMiles" stroke="#e8c96a" strokeWidth={2.5} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="analytics-card">
          <div className="analytics-card-header">
            <span className="analytics-title">Booking Type Mix</span>
            <span className="analytics-sub">Passenger behavior</span>
          </div>
          <div className="chart-shell">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={classBreakdown}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                <XAxis dataKey="name" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#22c55e" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="table-container">
        <div className="table-header"><span className="table-title">Passenger Bookings</span></div>
        <table>
          <thead>
            <tr><th>ID</th><th>Flight</th><th>Type</th><th>Kind</th><th>Date</th><th>Miles</th></tr>
          </thead>
          <tbody>
            {bookings.length === 0 ? (
              <tr><td colSpan={6}><div className="table-empty">No bookings for this passenger.</div></td></tr>
            ) : bookings.map((b) => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td>#{b.flightId}</td>
                <td><span className={`badge ${b.type === 'BUSINESS' ? 'badge-gold' : 'badge-blue'}`}>{b.type}</span></td>
                <td>{b.kind}</td>
                <td>{b.date}</td>
                <td>{flightById.get(b.flightId)?.miles || 0} mi</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
