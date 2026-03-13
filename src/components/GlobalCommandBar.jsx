import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Command, Plane, Users, Building2, Briefcase, BookOpen } from 'lucide-react';
import { useDashboardQuery } from '../hooks/queries';

export default function GlobalCommandBar() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const { data, isLoading: loading } = useDashboardQuery();

  useEffect(() => {
    const onKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setOpen((v) => !v);
      }
      if (e.key === 'Escape') setOpen(false);
    };

    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  const dataset = useMemo(() => {
    if (!data || !open) return [];
    return [
      ...(data.airlines || []).map((x) => ({
        type: 'airline',
        id: x.id,
        title: x.name,
        subtitle: x.shortName,
        path: '/airlines',
      })),
      ...(data.airports || []).map((x) => ({
        type: 'airport',
        id: x.id,
        title: x.name,
        subtitle: `${x.shortName} - ${x.country || 'N/A'}`,
        path: '/airports',
      })),
      ...(data.flights || []).map((x) => ({
        type: 'flight',
        id: x.id,
        title: `Flight #${x.id}`,
        subtitle: `${x.miles || 0} miles`,
        path: `/flights/${x.id}`,
      })),
      ...(data.passengers || []).map((x) => ({
        type: 'passenger',
        id: x.id,
        title: x.name,
        subtitle: `${x.status || 'Unknown'} - ${x.milesAccount?.flightMiles || 0} mi`,
        path: `/passengers/${x.id}`,
      })),
      ...(data.bookings || []).map((x) => ({
        type: 'booking',
        id: x.id,
        title: `Booking #${x.id}`,
        subtitle: `${x.kind} - ${x.type}`,
        path: '/bookings',
      })),
    ];
  }, [data, open]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return dataset.slice(0, 20);
    return dataset
      .filter((i) => [i.type, i.title, i.subtitle, String(i.id)].some((v) => (v || '').toLowerCase().includes(q)))
      .slice(0, 20);
  }, [dataset, query]);

  const iconForType = (type) => {
    if (type === 'airline') return <Briefcase size={14} />;
    if (type === 'airport') return <Building2 size={14} />;
    if (type === 'flight') return <Plane size={14} />;
    if (type === 'passenger') return <Users size={14} />;
    return <BookOpen size={14} />;
  };

  const onSelect = (item) => {
    setOpen(false);
    setQuery('');
    navigate(item.path);
  };

  if (!open) return null;

  return (
    <div className="command-overlay" onClick={() => setOpen(false)}>
      <div className="command-modal" onClick={(e) => e.stopPropagation()}>
        <div className="command-input-wrap">
          <Search size={16} />
          <input
            autoFocus
            className="command-input"
            placeholder="Search airlines, airports, flights, passengers, bookings..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <span className="command-kbd"><Command size={12} />K</span>
        </div>

        <div className="command-results">
          {loading ? (
            <div className="command-empty">Loading data...</div>
          ) : filtered.length === 0 ? (
            <div className="command-empty">No results found.</div>
          ) : filtered.map((item) => (
            <button
              key={`${item.type}-${item.id}`}
              className="command-item"
              onClick={() => onSelect(item)}
            >
              <span className="command-icon">{iconForType(item.type)}</span>
              <span className="command-copy">
                <strong>{item.title}</strong>
                <small>{item.subtitle}</small>
              </span>
              <span className="command-type">{item.type}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
