import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import StatCard from '../components/StatCard';
import { Plane, Building2, Users, BookOpen, Briefcase, TrendingUp } from 'lucide-react';
import { airlineService } from '../services/airlineService';
import { airportService } from '../services/airportService';
import { flightService } from '../services/flightService';
import { passengerService } from '../services/passengerService';
import { bookingService } from '../services/bookingService';

export default function Dashboard() {
  const [stats, setStats] = useState({ airlines: 0, airports: 0, flights: 0, passengers: 0, bookings: 0 });
  const [recentBookings, setRecentBookings] = useState([]);

  useEffect(() => {
    Promise.all([
      airlineService.getAll(),
      airportService.getAll(),
      flightService.getAll(),
      passengerService.getAll(),
      bookingService.getAll(),
    ]).then(([al, ap, fl, pa, bo]) => {
      setStats({
        airlines: al.data.length,
        airports: ap.data.length,
        flights: fl.data.length,
        passengers: pa.data.length,
        bookings: bo.data.length,
      });
      setRecentBookings(bo.data.slice(-5).reverse());
    }).catch(console.error);
  }, []);

  return (
    <Layout title="Dashboard" subtitle="Overview of your flight management system">
      <div className="welcome-banner">
        <h1>Welcome back to <span className="gold-text">AirPort</span> ✈</h1>
        <p>Here's a live overview of your entire flight management system.</p>
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
              <th>Flight</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {recentBookings.length === 0 ? (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No bookings yet</td></tr>
            ) : recentBookings.map(b => (
              <tr key={b.id}>
                <td>#{b.id}</td>
                <td>{b.kind}</td>
                <td>
                  <span className={`badge ${b.type === 'BUSINESS' ? 'badge-gold' : 'badge-blue'}`}>
                    {b.type}
                  </span>
                </td>
                <td>Passenger #{b.passengerId}</td>
                <td>Flight #{b.flightId}</td>
                <td>{b.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}