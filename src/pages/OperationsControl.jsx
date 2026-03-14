import { useState, useEffect, useCallback, useRef } from 'react';
import {
  Activity, AlertTriangle, RefreshCw, CheckCircle, Clock,
  Plane, Users, AlertCircle, ChevronDown, ChevronUp, XCircle,
  ArrowRight, UserCheck,
} from 'lucide-react';
import { operationsService } from '../services/operationsService';
import Layout from '../components/Layout';

// ─── helpers ────────────────────────────────────────────────────────────────

function fmtTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function fmtDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString([], {
    month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}

function pluralMins(n) {
  if (n == null) return '—';
  return `${n} min`;
}

function slaLabel(secs) {
  if (secs == null) return null;
  if (secs <= 0) return { text: 'BREACHED', cls: 'sla-breached' };
  const m = Math.floor(secs / 60);
  const s = secs % 60;
  return { text: `${m}m ${s}s`, cls: secs < 300 ? 'sla-warning' : 'sla-ok' };
}

const STATUS_CLS = {
  SCHEDULED: 'status-badge status-scheduled',
  DELAYED: 'status-badge status-delayed',
  BOARDING: 'status-badge status-boarding',
  DEPARTED: 'status-badge status-departed',
  LANDED: 'status-badge status-landed',
  CANCELLED: 'status-badge status-cancelled',
};

const SEVERITY_CLS = {
  LOW: 'alert-severity-low',
  MEDIUM: 'alert-severity-medium',
  HIGH: 'alert-severity-high',
  CRITICAL: 'alert-severity-critical',
};

// ─── Live Board ──────────────────────────────────────────────────────────────

function LiveBoard({ board, loading, onPropagateDelay }) {
  const [expanded, setExpanded] = useState(null);
  const [delayInput, setDelayInput] = useState({});

  const toggle = (id) => setExpanded((prev) => (prev === id ? null : id));

  return (
    <section className="ops-section">
      <div className="ops-section-header">
        <h2><Plane size={18} /> Live Flight Board</h2>
        <span className="ops-count">{board.length} flights</span>
      </div>

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      <div className="ops-board-table-wrap">
        <table className="ops-board-table">
          <thead>
            <tr>
              <th>Flight</th>
              <th>Route</th>
              <th>Sched. Dep</th>
              <th>Delay</th>
              <th>Status</th>
              <th>Gate</th>
              <th>Aircraft</th>
              <th>Seats</th>
              <th>Crew</th>
              <th>Turnaround</th>
              <th>Alerts</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {board.map((row) => {
              const isExpanded = expanded === row.flightId;
              const crewReady = row.crewReady;
              return (
                <>
                  <tr
                    key={row.flightId}
                    className={`ops-row ${row.activeAlertCount > 0 ? 'ops-row-alert' : ''}`}
                  >
                    <td className="ops-flight-id">#{row.flightId}</td>
                    <td className="ops-route">{row.route}</td>
                    <td>{fmtTime(row.scheduledDeparture)}</td>
                    <td className={row.delayMinutes > 0 ? 'ops-delay-cell' : ''}>
                      {row.delayMinutes > 0 ? `+${row.delayMinutes}m` : '—'}
                    </td>
                    <td><span className={STATUS_CLS[row.status] || 'status-badge'}>{row.status}</span></td>
                    <td>{row.gateNumber ?? '—'}</td>
                    <td className="ops-aircraft">
                      {row.aircraftRegistration
                        ? <><strong>{row.aircraftRegistration}</strong><br /><small>{row.aircraftModel}</small></>
                        : <span className="text-muted">unassigned</span>}
                    </td>
                    <td>{row.confirmedBookings ?? 0}/{row.totalSeats ?? '?'}</td>
                    <td className={crewReady ? 'crew-ready' : 'crew-not-ready'}>
                      <UserCheck size={13} />
                      {' '}{row.crewCheckedIn ?? 0}/{row.crewAssigned ?? 0}
                    </td>
                    <td>
                      {row.turnaroundMinutes != null ? (
                        <div className="turnaround-cell">
                          <div className="turnaround-bar-bg">
                            <div
                              className="turnaround-bar-fill"
                              style={{ width: `${Math.min(row.turnaroundPercentComplete, 100)}%` }}
                            />
                          </div>
                          <small>{row.turnaroundPercentComplete}%</small>
                        </div>
                      ) : '—'}
                    </td>
                    <td>
                      {row.activeAlertCount > 0 ? (
                        <span className={`alert-badge ${SEVERITY_CLS[row.highestAlertSeverity]}`}>
                          <AlertTriangle size={12} /> {row.activeAlertCount}
                        </span>
                      ) : (
                        <span className="alert-badge alert-badge-none"><CheckCircle size={12} /></span>
                      )}
                    </td>
                    <td>
                      <button
                        className="btn-icon"
                        onClick={() => toggle(row.flightId)}
                        title="Expand"
                      >
                        {isExpanded ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
                      </button>
                    </td>
                  </tr>

                  {isExpanded && (
                    <tr key={`${row.flightId}-detail`} className="ops-detail-row">
                      <td colSpan={12}>
                        <div className="ops-detail-panel">
                          {row.affectedConnectingFlights?.length > 0 && (
                            <div className="ops-detail-block">
                              <strong>Connecting Flights Affected:</strong>{' '}
                              {row.affectedConnectingFlights.join(', ')}
                            </div>
                          )}
                          <div className="ops-detail-block ops-delay-action">
                            <label>Propagate Delay:</label>
                            <input
                              type="number"
                              min={1}
                              max={600}
                              placeholder="min"
                              value={delayInput[row.flightId] ?? ''}
                              onChange={(e) =>
                                setDelayInput((p) => ({ ...p, [row.flightId]: e.target.value }))
                              }
                            />
                            <button
                              className="btn btn-primary btn-sm"
                              onClick={() => {
                                const mins = parseInt(delayInput[row.flightId], 10);
                                if (mins > 0) onPropagateDelay(row.flightId, mins);
                              }}
                            >
                              Apply
                            </button>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </>
              );
            })}
          </tbody>
        </table>
      </div>
    </section>
  );
}

// ─── Alerts Panel ────────────────────────────────────────────────────────────

function AlertsPanel({ alerts, loading, onResolve }) {
  const [resolveInput, setResolveInput] = useState({});
  const [slaCounters, setSlaCounters] = useState({});

  // Tick SLA countdown every second
  useEffect(() => {
    const tick = setInterval(() => {
      const updated = {};
      alerts.forEach((a) => {
        updated[a.id] = a.slaRemainingSeconds - 1;
      });
      setSlaCounters(updated);
    }, 1000);
    return () => clearInterval(tick);
  }, [alerts]);

  const effectiveSla = (a) =>
    slaCounters[a.id] != null ? slaCounters[a.id] : a.slaRemainingSeconds;

  return (
    <section className="ops-section">
      <div className="ops-section-header">
        <h2><AlertCircle size={18} /> Active Alerts</h2>
        <span className="ops-count">{alerts.filter((a) => !a.resolved).length} open</span>
      </div>

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      {alerts.length === 0 && !loading && (
        <div className="ops-empty"><CheckCircle size={20} /> No active alerts</div>
      )}

      <div className="alerts-list">
        {alerts.map((alert) => {
          const sla = slaLabel(effectiveSla(alert));
          return (
            <div key={alert.id} className={`alert-card ${SEVERITY_CLS[alert.severity]}`}>
              <div className="alert-card-header">
                <div className="alert-card-left">
                  <span className={`alert-severity-dot ${SEVERITY_CLS[alert.severity]}`} />
                  <strong>{alert.alertType?.replace(/_/g, ' ')}</strong>
                  {alert.flightRoute && (
                    <span className="alert-route">
                      <Plane size={11} /> {alert.flightRoute}
                    </span>
                  )}
                </div>
                <div className="alert-card-right">
                  {sla && (
                    <span className={`sla-chip ${sla.cls}`}>
                      <Clock size={11} /> {sla.text}
                    </span>
                  )}
                  <span className="alert-time">{fmtDateTime(alert.triggeredAt)}</span>
                </div>
              </div>
              <p className="alert-message">{alert.message}</p>
              {alert.details && <p className="alert-details">{alert.details}</p>}
              {!alert.resolved && (
                <div className="alert-resolve-row">
                  <input
                    className="alert-resolve-input"
                    placeholder="Resolution note…"
                    value={resolveInput[alert.id] ?? ''}
                    onChange={(e) =>
                      setResolveInput((p) => ({ ...p, [alert.id]: e.target.value }))
                    }
                  />
                  <button
                    className="btn btn-success btn-sm"
                    onClick={() => onResolve(alert.id, resolveInput[alert.id] || 'Resolved by operator')}
                  >
                    <CheckCircle size={13} /> Resolve
                  </button>
                </div>
              )}
              {alert.resolved && (
                <div className="alert-resolved-note">
                  <CheckCircle size={13} /> Resolved by {alert.resolvedBy} — {alert.resolution}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </section>
  );
}

// ─── Disruption Manager ───────────────────────────────────────────────────────

function DisruptionManager({ plans, loading, onRebook }) {
  const [expanded, setExpanded] = useState(null);

  const toggle = (id) => setExpanded((p) => (p === id ? null : id));

  return (
    <section className="ops-section">
      <div className="ops-section-header">
        <h2><XCircle size={18} /> Disruption Manager</h2>
        <span className="ops-count">{plans.length} disrupted</span>
      </div>

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      {plans.length === 0 && !loading && (
        <div className="ops-empty"><CheckCircle size={20} /> No disrupted flights</div>
      )}

      {plans.map((plan) => (
        <div key={plan.disruptedFlightId} className="disruption-card">
          <div
            className="disruption-card-header"
            onClick={() => toggle(plan.disruptedFlightId)}
            style={{ cursor: 'pointer' }}
          >
            <div className="disruption-card-title">
              <span className={STATUS_CLS[plan.disruptedFlightStatus] || 'status-badge'}>
                {plan.disruptedFlightStatus}
              </span>
              <strong>{plan.disruptedFlightRoute}</strong>
              {plan.delayMinutes > 0 && (
                <span className="ops-delay-cell">+{plan.delayMinutes}m delay</span>
              )}
            </div>
            <div className="disruption-affected">
              <Users size={13} /> {plan.affectedPassengerCount} passengers affected
            </div>
            <button className="btn-icon">
              {expanded === plan.disruptedFlightId ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
            </button>
          </div>

          {expanded === plan.disruptedFlightId && (
            <div className="disruption-detail">
              <div className="disruption-alternatives">
                <h4>Alternative Flights</h4>
                {plan.alternativeFlights?.length === 0 && (
                  <p className="text-muted">No alternatives available on the same route.</p>
                )}
                {plan.alternativeFlights?.map((alt) => (
                  <div key={alt.flightId} className="alt-flight">
                    <Plane size={13} />
                    <span>
                      #{alt.flightId} — {alt.route} @ {fmtTime(alt.scheduledDeparture)}
                    </span>
                    <span className={STATUS_CLS[alt.status] || 'status-badge'}>{alt.status}</span>
                    <span className="text-muted">{alt.seatsAvailable} seats avail.</span>
                  </div>
                ))}
              </div>
              <div className="disruption-bookings">
                <h4>Affected Bookings</h4>
                <table className="ops-board-table">
                  <thead>
                    <tr>
                      <th>Booking #</th>
                      <th>Passenger</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {plan.affectedBookings?.map((b) => (
                      <tr key={b.bookingId}>
                        <td>#{b.bookingId}</td>
                        <td>{b.passengerName}</td>
                        <td>
                          <span className={STATUS_CLS[b.bookingStatus] || 'status-badge'}>
                            {b.bookingStatus}
                          </span>
                        </td>
                        <td>
                          {plan.alternativeFlights?.length > 0 ? (
                            <select
                              className="rebook-select"
                              defaultValue=""
                              onChange={(e) => {
                                if (e.target.value) onRebook(b.bookingId, parseInt(e.target.value, 10));
                              }}
                            >
                              <option value="" disabled>Rebook to…</option>
                              {plan.alternativeFlights.map((alt) => (
                                <option key={alt.flightId} value={alt.flightId}>
                                  #{alt.flightId} {alt.route} ({alt.seatsAvailable} seats)
                                </option>
                              ))}
                            </select>
                          ) : (
                            <span className="text-muted">—</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      ))}
    </section>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function OperationsControl() {
  const [board, setBoard] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [disruptions, setDisruptions] = useState([]);
  const [loading, setLoading] = useState({ board: false, alerts: false, disruptions: false });
  const [error, setError] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [activeTab, setActiveTab] = useState('board');
  const intervalRef = useRef(null);

  const fetchAll = useCallback(async () => {
    setLoading({ board: true, alerts: true, disruptions: true });
    try {
      const [boardRes, alertsRes, disruptionsRes] = await Promise.all([
        operationsService.getLiveBoard(),
        operationsService.getAlerts(),
        operationsService.getDisruptions(),
      ]);
      setBoard(boardRes.data);
      setAlerts(alertsRes.data);
      setDisruptions(disruptionsRes.data);
      setError(null);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load operations data');
    } finally {
      setLoading({ board: false, alerts: false, disruptions: false });
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  useEffect(() => {
    if (autoRefresh) {
      intervalRef.current = setInterval(fetchAll, 30000);
    }
    return () => clearInterval(intervalRef.current);
  }, [autoRefresh, fetchAll]);

  const handlePropagateDelay = async (flightId, mins) => {
    try {
      await operationsService.propagateDelay(flightId, mins);
      fetchAll();
    } catch {
      setError('Failed to propagate delay');
    }
  };

  const handleResolve = async (alertId, resolution) => {
    try {
      await operationsService.resolveAlert(alertId, resolution);
      fetchAll();
    } catch {
      setError('Failed to resolve alert');
    }
  };

  const handleRebook = async (bookingId, newFlightId) => {
    try {
      await operationsService.executeRebooking(bookingId, newFlightId);
      fetchAll();
    } catch {
      setError('Failed to execute rebooking');
    }
  };

  const openAlerts = alerts.filter((a) => !a.resolved);
  const criticalCount = openAlerts.filter((a) => a.severity === 'CRITICAL').length;

  const tabs = [
    { id: 'board', label: 'Live Board', icon: Plane, badge: board.length },
    { id: 'alerts', label: 'Alerts', icon: AlertCircle, badge: openAlerts.length, badgeCritical: criticalCount > 0 },
    { id: 'disruptions', label: 'Disruptions', icon: XCircle, badge: disruptions.length },
  ];

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title"><Activity size={24} /> Operations Control</h1>
          <p className="page-subtitle">Real-time flight board, alerts with SLA timers, and disruption management</p>
        </div>
        <div className="page-header-actions">
          <label className="auto-refresh-toggle">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
            />
            Auto-refresh (30s)
          </label>
          <button className="btn btn-secondary btn-sm" onClick={fetchAll}>
            <RefreshCw size={14} /> Refresh
          </button>
        </div>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}

      <div className="ops-stat-row">
        <div className="ops-stat">
          <Plane size={16} /> <strong>{board.length}</strong> <span>Tracked</span>
        </div>
        <div className="ops-stat ops-stat-warn">
          <AlertTriangle size={16} /> <strong>{openAlerts.length}</strong> <span>Open Alerts</span>
        </div>
        <div className={`ops-stat ${criticalCount > 0 ? 'ops-stat-critical' : ''}`}>
          <AlertCircle size={16} /> <strong>{criticalCount}</strong> <span>Critical</span>
        </div>
        <div className="ops-stat ops-stat-danger">
          <XCircle size={16} /> <strong>{disruptions.length}</strong> <span>Disrupted</span>
        </div>
      </div>

      <div className="ops-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`ops-tab ${activeTab === tab.id ? 'ops-tab-active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <tab.icon size={15} />
            {tab.label}
            {tab.badge > 0 && (
              <span className={`ops-tab-badge ${tab.badgeCritical ? 'ops-tab-badge-critical' : ''}`}>
                {tab.badge}
              </span>
            )}
          </button>
        ))}
      </div>

      {activeTab === 'board' && (
        <LiveBoard
          board={board}
          loading={loading.board}
          onPropagateDelay={handlePropagateDelay}
        />
      )}
      {activeTab === 'alerts' && (
        <AlertsPanel
          alerts={openAlerts}
          loading={loading.alerts}
          onResolve={handleResolve}
        />
      )}
      {activeTab === 'disruptions' && (
        <DisruptionManager
          plans={disruptions}
          loading={loading.disruptions}
          onRebook={handleRebook}
        />
      )}
    </Layout>
  );
}
