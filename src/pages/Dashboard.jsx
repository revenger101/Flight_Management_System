import { useEffect, useMemo, useState } from 'react';
import Layout from '../components/Layout';
import StatCard from '../components/StatCard';
import { Plane, Building2, Users, BookOpen, Briefcase, TrendingUp, Activity, BarChart3, Route, PieChart as PieChartIcon, Clock3 } from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  AreaChart,
  Area,
} from 'recharts';
import { airlineService } from '../services/airlineService';
import { airportService } from '../services/airportService';
import { flightService } from '../services/flightService';
import { passengerService } from '../services/passengerService';
import { bookingService } from '../services/bookingService';

export default function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ airlines: 0, airports: 0, flights: 0, passengers: 0, bookings: 0 });
  const [airports, setAirports] = useState([]);
  const [flights, setFlights] = useState([]);
  const [passengers, setPassengers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [recentBookings, setRecentBookings] = useState([]);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      airlineService.getAll(),
      airportService.getAll(),
      flightService.getAll(),
      passengerService.getAll(),
      bookingService.getAll(),
    ]).then(([al, ap, fl, pa, bo]) => {
      setAirports(ap.data);
      setFlights(fl.data);
      setPassengers(pa.data);
      setBookings(bo.data);
      setStats({
        airlines: al.data.length,
        airports: ap.data.length,
        flights: fl.data.length,
        passengers: pa.data.length,
        bookings: bo.data.length,
      });
      setRecentBookings(
        [...bo.data]
          .sort((a, b) => (b.date || '').localeCompare(a.date || '') || (b.id || 0) - (a.id || 0))
          .slice(0, 7)
      );
    }).catch(console.error).finally(() => setLoading(false));
  }, []);

  const airportById = useMemo(() => {
    const m = new Map();
    airports.forEach((a) => m.set(a.id, a));
    return m;
  }, [airports]);

  const flightById = useMemo(() => {
    const m = new Map();
    flights.forEach((f) => m.set(f.id, f));
    return m;
  }, [flights]);

  const passengerById = useMemo(() => {
    const m = new Map();
    passengers.forEach((p) => m.set(p.id, p));
    return m;
  }, [passengers]);

  const bookingMix = useMemo(() => {
    const economic = bookings.filter((b) => b.type === 'ECONOMIC').length;
    const business = bookings.filter((b) => b.type === 'BUSINESS').length;
    const total = bookings.length || 1;
    const businessRate = Math.round((business / total) * 100);
    return { economic, business, businessRate };
  }, [bookings]);

  const bookingMixChartData = useMemo(() => ([
    { name: 'Business', value: bookingMix.business, color: '#c9a84c' },
    { name: 'Economic', value: bookingMix.economic, color: '#3b82f6' },
  ]), [bookingMix]);

  const statusDistribution = useMemo(() => {
    const base = ['Bronze', 'Silver', 'Gold', 'Platinum'];
    const counts = base.map((status) => ({
      status,
      count: passengers.filter((p) => p.status === status).length,
    }));
    const max = Math.max(1, ...counts.map((s) => s.count));
    return counts.map((s) => ({ ...s, width: Math.max(12, Math.round((s.count / max) * 100)) }));
  }, [passengers]);

  const bookingsByMonth = useMemo(() => {
    const now = new Date();
    const buckets = [];
    for (let i = 5; i >= 0; i -= 1) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      const label = d.toLocaleDateString(undefined, { month: 'short' });
      buckets.push({ key, label, value: 0 });
    }
    bookings.forEach((b) => {
      const key = (b.date || '').slice(0, 7);
      const bucket = buckets.find((x) => x.key === key);
      if (bucket) bucket.value += 1;
    });
    const max = Math.max(1, ...buckets.map((b) => b.value));
    return buckets.map((b) => ({ ...b, height: Math.max(10, Math.round((b.value / max) * 100)) }));
  }, [bookings]);

  const topRoutes = useMemo(() => {
    const map = new Map();
    bookings.forEach((b) => {
      const f = flightById.get(b.flightId);
      if (!f) return;
      const dep = airportById.get(f.departureAirportId)?.shortName || `#${f.departureAirportId}`;
      const arr = airportById.get(f.arrivalAirportId)?.shortName || `#${f.arrivalAirportId}`;
      const key = `${dep} -> ${arr}`;
      map.set(key, (map.get(key) || 0) + 1);
    });
    return [...map.entries()]
      .map(([route, count]) => ({ route, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  }, [bookings, airportById, flightById]);

  const statusChartData = useMemo(
    () => statusDistribution.map((s) => ({ status: s.status, count: s.count })),
    [statusDistribution]
  );

  const topRoutesChartData = useMemo(
    () => topRoutes.map((r) => ({ route: r.route, bookings: r.count })),
    [topRoutes]
  );

  const operationalKpi = useMemo(() => {
    const totalMiles = bookings.reduce((sum, b) => sum + Number(flightById.get(b.flightId)?.miles || 0), 0);
    const avgMiles = bookings.length ? Math.round(totalMiles / bookings.length) : 0;
    const activeAirports = new Set(
      flights.flatMap((f) => [f.departureAirportId, f.arrivalAirportId]).filter(Boolean)
    ).size;
    return { avgMiles, activeAirports };
  }, [bookings, flights, flightById]);

  return (
    <Layout title="Dashboard" subtitle="Overview of your flight management system">
      <div className="welcome-banner">
        <h1>Welcome back to <span className="gold-text">AirPort</span> ✈</h1>
        <p>Here's a live overview of your entire flight management system.</p>
        <div className="welcome-metrics">
          <span className="welcome-pill"><Activity size={13} /> Live Data</span>
          <span className="welcome-pill"><Clock3 size={13} /> Updated in real-time</span>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={<Briefcase size={18} />} value={stats.airlines} label="Airlines" />
        <StatCard icon={<Building2 size={18} />} value={stats.airports} label="Airports"
          color="var(--blue-accent)" bgColor="var(--blue-dim)" />
        <StatCard icon={<Plane size={18} />} value={stats.flights} label="Flights"
          color="#a78bfa" bgColor="rgba(167,139,250,0.1)" />
        <StatCard icon={<Users size={18} />} value={stats.passengers} label="Passengers"
          color="var(--success)" bgColor="rgba(34,197,94,0.1)" />
        <StatCard icon={<BookOpen size={18} />} value={stats.bookings} label="Bookings"
          color="var(--warning)" bgColor="rgba(245,158,11,0.1)" />
        <StatCard icon={<BarChart3 size={18} />} value={`${bookingMix.businessRate}%`} label="Business Share"
          color="#f97316" bgColor="rgba(249,115,22,0.14)" />
        <StatCard icon={<Route size={18} />} value={operationalKpi.avgMiles.toLocaleString()} label="Avg Miles / Booking"
          color="#22d3ee" bgColor="rgba(34,211,238,0.13)" />
      </div>

      {loading ? (
        <div className="table-container">
          <div className="loading-screen"><div className="spinner" /></div>
        </div>
      ) : (
        <>
          <div className="dashboard-analytics-grid">
            <div className="analytics-card analytics-card-wide">
              <div className="analytics-card-header">
                <span className="analytics-title"><PieChartIcon size={16} /> Booking Class Distribution</span>
                <span className="analytics-sub">ECONOMIC vs BUSINESS</span>
              </div>
              <div className="booking-mix-wrap">
                <div className="chart-shell chart-shell-pie">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={bookingMixChartData}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        innerRadius={54}
                        outerRadius={84}
                        paddingAngle={3}
                        stroke="none"
                      >
                        {bookingMixChartData.map((entry) => (
                          <Cell key={entry.name} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{
                          background: '#0d1420',
                          border: '1px solid #243350',
                          borderRadius: 10,
                          color: '#f0f4ff',
                        }}
                        formatter={(value) => [`${value} bookings`, 'Volume']}
                      />
                      <Legend iconType="circle" />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="donut-center-copy">
                  <div className="donut-value">{bookingMix.businessRate}%</div>
                  <div className="donut-label">Business class rate</div>
                </div>
                <div className="mix-legend">
                  <div className="mix-row">
                    <span className="dot dot-gold" />
                    <span>Business</span>
                    <strong>{bookingMix.business}</strong>
                  </div>
                  <div className="mix-row">
                    <span className="dot dot-blue" />
                    <span>Economic</span>
                    <strong>{bookingMix.economic}</strong>
                  </div>
                  <div className="mix-row">
                    <span className="dot dot-green" />
                    <span>Active Airports</span>
                    <strong>{operationalKpi.activeAirports}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div className="analytics-card">
              <div className="analytics-card-header">
                <span className="analytics-title"><Users size={16} /> Passenger Tiers</span>
                <span className="analytics-sub">Loyalty distribution</span>
              </div>
              <div className="chart-shell">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={statusChartData} layout="vertical" margin={{ top: 6, right: 8, left: 8, bottom: 6 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                    <XAxis type="number" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <YAxis type="category" dataKey="status" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} width={62} />
                    <Tooltip
                      contentStyle={{
                        background: '#0d1420',
                        border: '1px solid #243350',
                        borderRadius: 10,
                        color: '#f0f4ff',
                      }}
                      formatter={(value) => [value, 'Passengers']}
                    />
                    <Bar dataKey="count" radius={[0, 6, 6, 0]} fill="#c9a84c" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="analytics-card">
              <div className="analytics-card-header">
                <span className="analytics-title"><TrendingUp size={16} /> 6-Month Booking Trend</span>
                <span className="analytics-sub">Monthly volume</span>
              </div>
              <div className="chart-shell">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={bookingsByMonth} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="bookingGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#c9a84c" stopOpacity={0.45} />
                        <stop offset="95%" stopColor="#c9a84c" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                    <XAxis dataKey="label" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <YAxis tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                    <Tooltip
                      contentStyle={{
                        background: '#0d1420',
                        border: '1px solid #243350',
                        borderRadius: 10,
                        color: '#f0f4ff',
                      }}
                      formatter={(value) => [value, 'Bookings']}
                    />
                    <Area type="monotone" dataKey="value" stroke="#e8c96a" strokeWidth={2.5} fill="url(#bookingGradient)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="analytics-card">
              <div className="analytics-card-header">
                <span className="analytics-title"><Route size={16} /> Top Routes</span>
                <span className="analytics-sub">Most booked lanes</span>
              </div>
              {topRoutesChartData.length === 0 ? (
                <div className="table-empty" style={{ padding: '10px 0' }}>No route data yet</div>
              ) : (
                <div className="chart-shell">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={topRoutesChartData} layout="vertical" margin={{ top: 8, right: 8, left: 12, bottom: 4 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#1e2d45" />
                      <XAxis type="number" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} allowDecimals={false} />
                      <YAxis type="category" dataKey="route" tick={{ fill: '#8899bb', fontSize: 11 }} axisLine={false} tickLine={false} width={96} />
                      <Tooltip
                        contentStyle={{
                          background: '#0d1420',
                          border: '1px solid #243350',
                          borderRadius: 10,
                          color: '#f0f4ff',
                        }}
                        formatter={(value) => [`${value} bookings`, 'Traffic']}
                      />
                      <Bar dataKey="bookings" radius={[0, 6, 6, 0]} fill="#3b82f6" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              )}
            </div>
          </div>

          <div className="table-container">
            <div className="table-header">
              <span className="table-title">Recent Bookings</span>
              <TrendingUp size={16} style={{ color: 'var(--text-muted)' }} />
            </div>
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Kind</th>
                  <th>Type</th>
                  <th>Passenger</th>
                  <th>Route</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {recentBookings.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No bookings yet</td></tr>
                ) : recentBookings.map((b) => {
                  const passenger = passengerById.get(b.passengerId);
                  const flight = flightById.get(b.flightId);
                  const dep = airportById.get(flight?.departureAirportId)?.shortName;
                  const arr = airportById.get(flight?.arrivalAirportId)?.shortName;
                  return (
                    <tr key={b.id}>
                      <td>#{b.id}</td>
                      <td>{b.kind}</td>
                      <td>
                        <span className={`badge ${b.type === 'BUSINESS' ? 'badge-gold' : 'badge-blue'}`}>
                          {b.type}
                        </span>
                      </td>
                      <td>{passenger?.name || `Passenger #${b.passengerId}`}</td>
                      <td>{dep && arr ? `${dep} -> ${arr}` : `Flight #${b.flightId}`}</td>
                      <td>{b.date}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </Layout>
  );
}