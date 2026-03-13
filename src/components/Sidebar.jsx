import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Plane, Building2, Users,
  BookOpen, Briefcase, Wifi, ChevronsUpDown, LogOut
} from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const navigationSections = [
  {
    label: 'Main',
    items: [
      { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
      { to: '/flights', icon: Plane, label: 'Flights' },
      { to: '/passengers', icon: Users, label: 'Passengers' },
      { to: '/bookings', icon: BookOpen, label: 'Bookings' },
    ],
  },
  {
    label: 'Admin',
    items: [
      { to: '/airports', icon: Building2, label: 'Airports', roles: ['ADMIN'] },
      { to: '/airlines', icon: Briefcase, label: 'Airlines', roles: ['ADMIN'] },
    ],
  },
];

export default function Sidebar() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const initials = (user?.fullName || user?.email || 'User')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('');

  const visibleSections = navigationSections
    .map((section) => ({
      ...section,
      items: section.items.filter((item) => !item.roles || item.roles.includes(user?.role)),
    }))
    .filter((section) => section.items.length > 0);

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

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
        {visibleSections.map((section) => (
          <div key={section.label}>
            <div className="nav-section-label">{section.label}</div>
            {section.items.map(({ to, icon: Icon, label }) => (
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
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        {user && (
          <details className="sidebar-account">
            <summary className="sidebar-account-trigger">
              <div className="sidebar-account-avatar">{initials || 'U'}</div>
              <div className="sidebar-account-copy">
                <strong>{user.fullName || 'Signed In'}</strong>
                <span>{user.email}</span>
              </div>
              <ChevronsUpDown size={16} />
            </summary>
            <div className="sidebar-account-menu">
              <div className="sidebar-account-badges">
                <span className="sidebar-badge sidebar-badge-role">{user.role || 'USER'}</span>
                <span className="sidebar-badge sidebar-badge-provider">{user.provider || 'LOCAL'}</span>
              </div>
              <p className="sidebar-account-caption">Professional session active on this device.</p>
              <button className="btn btn-secondary btn-sm sidebar-account-action" onClick={handleLogout} type="button">
                <LogOut size={14} />
                Sign out
              </button>
            </div>
          </details>
        )}
        <div className="system-status">
          <div className="status-dot" />
          <Wifi size={13} />
          <span>API Connected</span>
        </div>
      </div>
    </aside>
  );
}