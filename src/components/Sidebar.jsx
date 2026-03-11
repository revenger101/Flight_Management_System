import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Plane, Building2, Users,
  BookOpen, Briefcase, Wifi
} from 'lucide-react';

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/flights', icon: Plane, label: 'Flights' },
  { to: '/airports', icon: Building2, label: 'Airports' },
  { to: '/airlines', icon: Briefcase, label: 'Airlines' },
  { to: '/passengers', icon: Users, label: 'Passengers' },
  { to: '/bookings', icon: BookOpen, label: 'Bookings' },
];

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-mark">
          <div className="logo-icon">✈</div>
          <div>
            <div className="logo-text">Air<span>Port</span></div>
            <div className="logo-sub">Management System</div>
          </div>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-section-label">Main</div>
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <Icon size={17} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="system-status">
          <div className="status-dot" />
          <Wifi size={13} />
          <span>API Connected</span>
        </div>
      </div>
    </aside>
  );
}