import { useMemo, useState } from 'react';
import { Bell, AlertTriangle, Plane, CalendarX2 } from 'lucide-react';
import { withOpsAndCapacity } from '../utils/flightOpsStore';
import { useBookingsQuery, useFlightsQuery } from '../hooks/queries';

export default function NotificationsCenter() {
  const [open, setOpen] = useState(false);
  const { data: flights = [] } = useFlightsQuery();
  const { data: bookings = [] } = useBookingsQuery();

  const alerts = useMemo(() => {
    const list = [];
    const opsFlights = withOpsAndCapacity(flights, bookings);

    opsFlights
      .filter((f) => f.status === 'Delayed' && Number(f.delayMinutes || 0) >= 30)
      .slice(0, 3)
      .forEach((f) => {
        list.push({
          type: 'delay',
          icon: AlertTriangle,
          title: `High delay on Flight #${f.id}`,
          message: `${f.delayMinutes} min delay`,
        });
      });

    opsFlights
      .filter((f) => f.seatCapacity > 0 && (f.availableSeats / f.seatCapacity) <= 0.15)
      .slice(0, 3)
      .forEach((f) => {
        list.push({
          type: 'capacity',
          icon: Plane,
          title: `Low seat availability on Flight #${f.id}`,
          message: `${f.availableSeats} seats left`,
        });
      });

    const now = new Date();
    const weekStart = new Date(now);
    weekStart.setDate(now.getDate() - now.getDay());
    weekStart.setHours(0, 0, 0, 0);

    const hasBookingThisWeek = bookings.some((b) => {
      const d = new Date(b.date);
      return !Number.isNaN(d.getTime()) && d >= weekStart;
    });

    if (!hasBookingThisWeek) {
      list.push({
        type: 'booking',
        icon: CalendarX2,
        title: 'No bookings this week',
        message: 'Consider promotions or schedule updates.',
      });
    }

    return list.slice(0, 8);
  }, [flights, bookings]);

  return (
    <div className="notifications-wrap">
      <button className="notifications-trigger" onClick={() => setOpen((v) => !v)}>
        <Bell size={15} />
        {alerts.length > 0 && <span className="notifications-badge">{alerts.length}</span>}
      </button>

      {open && (
        <div className="notifications-panel">
          <div className="notifications-header">
            <strong>Notifications</strong>
            <span>{alerts.length} alert(s)</span>
          </div>

          <div className="notifications-list">
            {alerts.length === 0 ? (
              <div className="notifications-empty">No active alerts.</div>
            ) : alerts.map((alert, index) => {
              const Icon = alert.icon;
              return (
                <div key={`${alert.type}-${index}`} className="notification-item">
                  <span className={`notification-icon notification-${alert.type}`}><Icon size={14} /></span>
                  <div>
                    <div className="notification-title">{alert.title}</div>
                    <div className="notification-message">{alert.message}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
