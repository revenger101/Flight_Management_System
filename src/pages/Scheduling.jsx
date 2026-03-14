import { useState, useEffect, useCallback } from 'react';
import {
  Calendar, Plane, Users, Grid, RefreshCw, PlusCircle,
  Pencil, Trash2, CheckCircle, AlertTriangle, UserCheck,
  XCircle, ChevronDown, ChevronUp,
} from 'lucide-react';
import { schedulingService } from '../services/schedulingService';
import Layout from '../components/Layout';

// ─── helpers ──────────────────────────────────────────────────────────────────

function fmtDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString([], {
    month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit',
  });
}

const STATUS_CLS = {
  AVAILABLE: 'status-badge status-landed',
  IN_SERVICE: 'status-badge status-departed',
  MAINTENANCE: 'status-badge status-delayed',
  GROUNDED: 'status-badge status-cancelled',
};

const GATE_STATUS_CLS = {
  SCHEDULED: 'status-badge status-scheduled',
  ACTIVE: 'status-badge status-boarding',
  COMPLETED: 'status-badge status-landed',
  CANCELLED: 'status-badge status-cancelled',
};

// ─── Aircraft Tab ─────────────────────────────────────────────────────────────

function AircraftTab({ aircraft, loading, onDelete, onAssign, onRefresh }) {
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    registration: '', model: '', manufacturer: '',
    totalSeats: '', economySeats: '', businessSeats: '',
    status: 'AVAILABLE', airlineCode: '',
  });
  const [assigning, setAssigning] = useState(null);
  const [flightIdInput, setFlightIdInput] = useState('');

  const handleCreate = async () => {
    try {
      await schedulingService.createAircraft({
        ...form,
        totalSeats: parseInt(form.totalSeats, 10),
        economySeats: parseInt(form.economySeats, 10),
        businessSeats: parseInt(form.businessSeats, 10),
      });
      setShowForm(false);
      setForm({ registration: '', model: '', manufacturer: '', totalSeats: '', economySeats: '', businessSeats: '', status: 'AVAILABLE', airlineCode: '' });
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to create aircraft');
    }
  };

  const handleAssign = async (acId) => {
    const fid = parseInt(flightIdInput, 10);
    if (!fid) return;
    try {
      await schedulingService.assignAircraftToFlight(acId, fid);
      setAssigning(null);
      setFlightIdInput('');
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to assign aircraft');
    }
  };

  return (
    <div>
      <div className="sched-tab-toolbar">
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm((p) => !p)}>
          <PlusCircle size={14} /> Add Aircraft
        </button>
      </div>

      {showForm && (
        <div className="sched-form-card">
          <h4>New Aircraft</h4>
          <div className="sched-form-grid">
            {[['registration', 'Registration'], ['model', 'Model'], ['manufacturer', 'Manufacturer'], ['airlineCode', 'Airline Code']].map(([k, l]) => (
              <label key={k}>
                {l}
                <input value={form[k]} onChange={(e) => setForm((p) => ({ ...p, [k]: e.target.value }))} />
              </label>
            ))}
            {[['totalSeats', 'Total Seats'], ['economySeats', 'Economy'], ['businessSeats', 'Business']].map(([k, l]) => (
              <label key={k}>
                {l}
                <input type="number" value={form[k]} onChange={(e) => setForm((p) => ({ ...p, [k]: e.target.value }))} />
              </label>
            ))}
            <label>
              Status
              <select value={form.status} onChange={(e) => setForm((p) => ({ ...p, status: e.target.value }))}>
                {['AVAILABLE', 'IN_SERVICE', 'MAINTENANCE', 'GROUNDED'].map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </label>
          </div>
          <div className="sched-form-actions">
            <button className="btn btn-primary btn-sm" onClick={handleCreate}>Save</button>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      <table className="ops-board-table">
        <thead>
          <tr>
            <th>Registration</th>
            <th>Model</th>
            <th>Manufacturer</th>
            <th>Seats</th>
            <th>Airline</th>
            <th>Status</th>
            <th>Next Maint.</th>
            <th>Flight Hours</th>
            <th>Current Flight</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {aircraft.map((ac) => (
            <tr key={ac.id}>
              <td><strong>{ac.registration}</strong></td>
              <td>{ac.model}</td>
              <td>{ac.manufacturer}</td>
              <td>
                {ac.totalSeats} <small className="text-muted">({ac.economySeats}Y/{ac.businessSeats}J)</small>
              </td>
              <td>{ac.airlineCode}</td>
              <td><span className={STATUS_CLS[ac.status] || 'status-badge'}>{ac.status}</span></td>
              <td>{fmtDateTime(ac.nextMaintenanceAt)}</td>
              <td>{ac.totalFlightHours?.toLocaleString() ?? '—'} h</td>
              <td>
                {ac.currentFlightId
                  ? <span>#{ac.currentFlightId} {ac.currentFlightRoute}</span>
                  : <span className="text-muted">—</span>}
              </td>
              <td className="sched-actions">
                {assigning === ac.id ? (
                  <>
                    <input
                      type="number"
                      placeholder="Flight ID"
                      value={flightIdInput}
                      onChange={(e) => setFlightIdInput(e.target.value)}
                      style={{ width: 80 }}
                    />
                    <button className="btn btn-primary btn-sm" onClick={() => handleAssign(ac.id)}>Assign</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => setAssigning(null)}>Cancel</button>
                  </>
                ) : (
                  <>
                    <button className="btn-icon" title="Assign to flight" onClick={() => setAssigning(ac.id)}>
                      <Plane size={14} />
                    </button>
                    <button className="btn-icon btn-icon-danger" title="Delete" onClick={() => onDelete(ac.id)}>
                      <Trash2 size={14} />
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ─── Crew Tab ─────────────────────────────────────────────────────────────────

function CrewTab({ crew, loading, onDelete, onRefresh }) {
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: '', role: 'CABIN_CREW', licenseNumber: '', nationality: '', email: '', phone: '',
  });

  const handleCreate = async () => {
    try {
      await schedulingService.createCrewMember(form);
      setShowForm(false);
      setForm({ name: '', role: 'CABIN_CREW', licenseNumber: '', nationality: '', email: '', phone: '' });
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to create crew member');
    }
  };

  const dutyPct = (cm) => {
    const max = cm.maxDutyMinutesPerCycle || 840;
    return Math.round(((cm.dutyMinutesThisCycle || 0) / max) * 100);
  };

  return (
    <div>
      <div className="sched-tab-toolbar">
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm((p) => !p)}>
          <PlusCircle size={14} /> Add Crew Member
        </button>
      </div>

      {showForm && (
        <div className="sched-form-card">
          <h4>New Crew Member</h4>
          <div className="sched-form-grid">
            {[['name', 'Full Name'], ['licenseNumber', 'License No.'], ['nationality', 'Nationality'], ['email', 'Email'], ['phone', 'Phone']].map(([k, l]) => (
              <label key={k}>
                {l}
                <input value={form[k]} onChange={(e) => setForm((p) => ({ ...p, [k]: e.target.value }))} />
              </label>
            ))}
            <label>
              Role
              <select value={form.role} onChange={(e) => setForm((p) => ({ ...p, role: e.target.value }))}>
                {['CAPTAIN', 'FIRST_OFFICER', 'PURSER', 'CABIN_CREW', 'GROUND_CREW', 'TRAINING_CAPTAIN'].map((r) => (
                  <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </label>
          </div>
          <div className="sched-form-actions">
            <button className="btn btn-primary btn-sm" onClick={handleCreate}>Save</button>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      <table className="ops-board-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Role</th>
            <th>License</th>
            <th>Nationality</th>
            <th>Duty Cycle</th>
            <th>Status</th>
            <th>Rest Until</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {crew.map((cm) => {
            const pct = dutyPct(cm);
            return (
              <tr key={cm.id}>
                <td><strong>{cm.name}</strong></td>
                <td><span className="crew-role-badge">{cm.role?.replace(/_/g, ' ')}</span></td>
                <td>{cm.licenseNumber}</td>
                <td>{cm.nationality}</td>
                <td>
                  <div className="duty-bar-wrap">
                    <div className="duty-bar-bg">
                      <div
                        className={`duty-bar-fill ${pct >= 90 ? 'duty-bar-critical' : pct >= 70 ? 'duty-bar-warn' : ''}`}
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <small>{cm.dutyMinutesThisCycle}/{cm.maxDutyMinutesPerCycle} min</small>
                  </div>
                </td>
                <td>
                  {cm.available
                    ? <span className="status-badge status-landed"><CheckCircle size={12} /> Available</span>
                    : <span className="status-badge status-cancelled"><XCircle size={12} /> Unavailable</span>}
                  {cm.dutyLimitReached && (
                    <span className="crew-compliance-badge compliance-fail">Duty Limit</span>
                  )}
                </td>
                <td>{cm.restPeriodActive ? fmtDateTime(cm.restPeriodEnd) : '—'}</td>
                <td>
                  <button className="btn-icon btn-icon-danger" title="Delete" onClick={() => onDelete(cm.id)}>
                    <Trash2 size={14} />
                  </button>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// ─── Roster Tab ───────────────────────────────────────────────────────────────

function RosterTab({ loading, onRefresh }) {
  const [flightId, setFlightId] = useState('');
  const [roster, setRoster] = useState([]);
  const [rosterLoading, setRosterLoading] = useState(false);
  const [showAssignForm, setShowAssignForm] = useState(false);
  const [assignForm, setAssignForm] = useState({ crewMemberId: '', flightId: '', roleOnFlight: 'CABIN_CREW', estimatedDutyMinutes: 120 });

  const fetchRoster = async () => {
    if (!flightId) return;
    setRosterLoading(true);
    try {
      const res = await schedulingService.getRosterByFlight(flightId);
      setRoster(res.data);
    } catch {
      setRoster([]);
    } finally {
      setRosterLoading(false);
    }
  };

  const handleCheckIn = async (rosterId) => {
    try {
      await schedulingService.checkInCrew(rosterId);
      fetchRoster();
    } catch (e) {
      alert(e?.response?.data?.message || 'Check-in failed');
    }
  };

  const handleRemove = async (rosterId) => {
    if (!window.confirm('Remove this crew member from the flight?')) return;
    try {
      await schedulingService.removeCrewFromFlight(rosterId);
      fetchRoster();
    } catch (e) {
      alert(e?.response?.data?.message || 'Remove failed');
    }
  };

  const handleAssign = async () => {
    try {
      await schedulingService.assignCrewToFlight({
        crewMemberId: parseInt(assignForm.crewMemberId, 10),
        flightId: parseInt(assignForm.flightId, 10),
        roleOnFlight: assignForm.roleOnFlight,
        estimatedDutyMinutes: parseInt(assignForm.estimatedDutyMinutes, 10),
      });
      setShowAssignForm(false);
      fetchRoster();
    } catch (e) {
      alert(e?.response?.data?.message || 'Assignment failed');
    }
  };

  return (
    <div>
      <div className="sched-tab-toolbar">
        <div className="roster-search-row">
          <label>Flight ID:</label>
          <input
            type="number"
            value={flightId}
            onChange={(e) => setFlightId(e.target.value)}
            placeholder="Enter flight ID"
            style={{ width: 120 }}
          />
          <button className="btn btn-primary btn-sm" onClick={fetchRoster}>
            Load Roster
          </button>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={() => setShowAssignForm((p) => !p)}>
          <PlusCircle size={14} /> Assign Crew
        </button>
      </div>

      {showAssignForm && (
        <div className="sched-form-card">
          <h4>Assign Crew to Flight</h4>
          <div className="sched-form-grid">
            <label>
              Crew Member ID
              <input
                type="number"
                value={assignForm.crewMemberId}
                onChange={(e) => setAssignForm((p) => ({ ...p, crewMemberId: e.target.value }))}
              />
            </label>
            <label>
              Flight ID
              <input
                type="number"
                value={assignForm.flightId}
                onChange={(e) => setAssignForm((p) => ({ ...p, flightId: e.target.value }))}
              />
            </label>
            <label>
              Role
              <select
                value={assignForm.roleOnFlight}
                onChange={(e) => setAssignForm((p) => ({ ...p, roleOnFlight: e.target.value }))}
              >
                {['CAPTAIN', 'FIRST_OFFICER', 'PURSER', 'CABIN_CREW', 'GROUND_CREW', 'TRAINING_CAPTAIN'].map((r) => (
                  <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
                ))}
              </select>
            </label>
            <label>
              Est. Duty (min)
              <input
                type="number"
                value={assignForm.estimatedDutyMinutes}
                onChange={(e) => setAssignForm((p) => ({ ...p, estimatedDutyMinutes: e.target.value }))}
              />
            </label>
          </div>
          <div className="sched-form-actions">
            <button className="btn btn-primary btn-sm" onClick={handleAssign}>Assign</button>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowAssignForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      {rosterLoading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading roster…</div>}

      {roster.length === 0 && !rosterLoading && flightId && (
        <div className="ops-empty">No crew assigned to flight #{flightId}</div>
      )}

      {roster.length > 0 && (
        <table className="ops-board-table">
          <thead>
            <tr>
              <th>Crew Member</th>
              <th>Role</th>
              <th>Est. Duty</th>
              <th>Duty Compliant</th>
              <th>Rest Compliant</th>
              <th>Checked In</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {roster.map((r) => (
              <tr key={r.id}>
                <td><strong>{r.crewMemberName}</strong></td>
                <td><span className="crew-role-badge">{r.roleOnFlight?.replace(/_/g, ' ')}</span></td>
                <td>{r.estimatedDutyMinutes} min</td>
                <td>
                  {r.dutyTimeCompliant
                    ? <span className="crew-compliance-badge compliance-ok"><CheckCircle size={12} /> OK</span>
                    : <span className="crew-compliance-badge compliance-fail"><AlertTriangle size={12} /> Over Limit</span>}
                </td>
                <td>
                  {r.restRuleCompliant
                    ? <span className="crew-compliance-badge compliance-ok"><CheckCircle size={12} /> OK</span>
                    : <span className="crew-compliance-badge compliance-fail"><AlertTriangle size={12} /> Rest Deficit</span>}
                </td>
                <td>
                  {r.checkedIn
                    ? <span className="status-badge status-landed"><UserCheck size={12} /> {fmtDateTime(r.checkedInAt)}</span>
                    : <span className="status-badge status-scheduled">Pending</span>}
                </td>
                <td className="sched-actions">
                  {!r.checkedIn && (
                    <button className="btn btn-success btn-sm" onClick={() => handleCheckIn(r.id)}>
                      <UserCheck size={13} /> Check In
                    </button>
                  )}
                  <button className="btn-icon btn-icon-danger" onClick={() => handleRemove(r.id)}>
                    <Trash2 size={14} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

// ─── Gates Tab ────────────────────────────────────────────────────────────────

function GatesTab({ gates, loading, onRefresh }) {
  const [airportIdInput, setAirportIdInput] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    airportId: '', gateNumber: '', flightId: '',
    scheduledStart: '', scheduledEnd: '', status: 'SCHEDULED',
  });
  const [detectAirportId, setDetectAirportId] = useState('');

  const handleCreate = async () => {
    try {
      await schedulingService.createGateSlot({
        airportId: parseInt(form.airportId, 10),
        gateNumber: parseInt(form.gateNumber, 10),
        flightId: parseInt(form.flightId, 10),
        scheduledStart: form.scheduledStart ? new Date(form.scheduledStart).toISOString() : undefined,
        scheduledEnd: form.scheduledEnd ? new Date(form.scheduledEnd).toISOString() : undefined,
        status: form.status,
      });
      setShowForm(false);
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Failed to create gate slot');
    }
  };

  const handleDetect = async () => {
    if (!detectAirportId) return;
    try {
      const res = await schedulingService.detectConflicts(parseInt(detectAirportId, 10));
      alert(`${res.data} conflict(s) detected and marked.`);
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Detection failed');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this gate slot?')) return;
    try {
      await schedulingService.deleteGateSlot(id);
      onRefresh();
    } catch (e) {
      alert(e?.response?.data?.message || 'Delete failed');
    }
  };

  return (
    <div>
      <div className="sched-tab-toolbar">
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm((p) => !p)}>
          <PlusCircle size={14} /> Add Gate Slot
        </button>
        <div className="roster-search-row">
          <label>Detect conflicts for airport ID:</label>
          <input
            type="number"
            value={detectAirportId}
            onChange={(e) => setDetectAirportId(e.target.value)}
            style={{ width: 80 }}
          />
          <button className="btn btn-secondary btn-sm" onClick={handleDetect}>
            <AlertTriangle size={14} /> Detect
          </button>
        </div>
      </div>

      {showForm && (
        <div className="sched-form-card">
          <h4>New Gate Slot</h4>
          <div className="sched-form-grid">
            <label>Airport ID<input type="number" value={form.airportId} onChange={(e) => setForm((p) => ({ ...p, airportId: e.target.value }))} /></label>
            <label>Gate No.<input type="number" value={form.gateNumber} onChange={(e) => setForm((p) => ({ ...p, gateNumber: e.target.value }))} /></label>
            <label>Flight ID<input type="number" value={form.flightId} onChange={(e) => setForm((p) => ({ ...p, flightId: e.target.value }))} /></label>
            <label>Start<input type="datetime-local" value={form.scheduledStart} onChange={(e) => setForm((p) => ({ ...p, scheduledStart: e.target.value }))} /></label>
            <label>End<input type="datetime-local" value={form.scheduledEnd} onChange={(e) => setForm((p) => ({ ...p, scheduledEnd: e.target.value }))} /></label>
            <label>
              Status
              <select value={form.status} onChange={(e) => setForm((p) => ({ ...p, status: e.target.value }))}>
                {['SCHEDULED', 'ACTIVE', 'COMPLETED', 'CANCELLED'].map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </label>
          </div>
          <div className="sched-form-actions">
            <button className="btn btn-primary btn-sm" onClick={handleCreate}>Save</button>
            <button className="btn btn-secondary btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </div>
      )}

      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading…</div>}

      <table className="ops-board-table">
        <thead>
          <tr>
            <th>Airport</th>
            <th>Gate</th>
            <th>Flight</th>
            <th>Sched. Start</th>
            <th>Sched. End</th>
            <th>Actual Start</th>
            <th>Status</th>
            <th>Conflict</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {gates.map((g) => (
            <tr key={g.id} className={g.conflict ? 'gate-conflict-row' : ''}>
              <td>{g.airportCode || `AP#${g.airportId}`}</td>
              <td><strong>Gate {g.gateNumber}</strong></td>
              <td>{g.flightRoute ? `#${g.flightId} ${g.flightRoute}` : `#${g.flightId}`}</td>
              <td>{fmtDateTime(g.scheduledStart)}</td>
              <td>{fmtDateTime(g.scheduledEnd)}</td>
              <td>{fmtDateTime(g.actualStart)}</td>
              <td><span className={GATE_STATUS_CLS[g.status] || 'status-badge'}>{g.status}</span></td>
              <td>
                {g.conflict
                  ? <span className="crew-compliance-badge compliance-fail"><AlertTriangle size={12} /> Conflict #{g.conflictingSlotId}</span>
                  : <span className="crew-compliance-badge compliance-ok"><CheckCircle size={12} /> Clear</span>}
              </td>
              <td>
                <button className="btn-icon btn-icon-danger" onClick={() => handleDelete(g.id)}>
                  <Trash2 size={14} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

export default function Scheduling() {
  const [activeTab, setActiveTab] = useState('aircraft');
  const [aircraft, setAircraft] = useState([]);
  const [crew, setCrew] = useState([]);
  const [gates, setGates] = useState([]);
  const [loading, setLoading] = useState({ aircraft: false, crew: false, gates: false });
  const [error, setError] = useState(null);

  const fetchAircraft = useCallback(async () => {
    setLoading((p) => ({ ...p, aircraft: true }));
    try {
      const res = await schedulingService.getAircraft();
      setAircraft(res.data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load aircraft');
    } finally {
      setLoading((p) => ({ ...p, aircraft: false }));
    }
  }, []);

  const fetchCrew = useCallback(async () => {
    setLoading((p) => ({ ...p, crew: true }));
    try {
      const res = await schedulingService.getCrew();
      setCrew(res.data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load crew');
    } finally {
      setLoading((p) => ({ ...p, crew: false }));
    }
  }, []);

  const fetchGates = useCallback(async () => {
    setLoading((p) => ({ ...p, gates: true }));
    try {
      const res = await schedulingService.getGateSlots();
      setGates(res.data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load gate slots');
    } finally {
      setLoading((p) => ({ ...p, gates: false }));
    }
  }, []);

  useEffect(() => {
    fetchAircraft();
    fetchCrew();
    fetchGates();
  }, [fetchAircraft, fetchCrew, fetchGates]);

  const handleDeleteAircraft = async (id) => {
    if (!window.confirm('Delete this aircraft?')) return;
    try {
      await schedulingService.deleteAircraft(id);
      fetchAircraft();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to delete aircraft');
    }
  };

  const handleDeleteCrew = async (id) => {
    if (!window.confirm('Delete this crew member?')) return;
    try {
      await schedulingService.deleteCrewMember(id);
      fetchCrew();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to delete crew member');
    }
  };

  const tabs = [
    { id: 'aircraft', label: 'Aircraft Fleet', icon: Plane, badge: aircraft.length },
    { id: 'crew', label: 'Crew Members', icon: Users, badge: crew.length },
    { id: 'roster', label: 'Crew Roster', icon: UserCheck },
    { id: 'gates', label: 'Gate Slots', icon: Grid, badge: gates.filter((g) => g.conflict).length, badgeCritical: gates.some((g) => g.conflict) },
  ];

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title"><Calendar size={24} /> Scheduling & Resources</h1>
          <p className="page-subtitle">Aircraft assignment, crew roster management, gate and slot optimization</p>
        </div>
        <div className="page-header-actions">
          <button className="btn btn-secondary btn-sm" onClick={() => { fetchAircraft(); fetchCrew(); fetchGates(); }}>
            <RefreshCw size={14} /> Refresh All
          </button>
        </div>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}

      <div className="ops-stat-row">
        <div className="ops-stat">
          <Plane size={16} /> <strong>{aircraft.length}</strong> <span>Aircraft</span>
        </div>
        <div className="ops-stat">
          <Users size={16} /> <strong>{crew.length}</strong> <span>Crew Members</span>
        </div>
        <div className={`ops-stat ${crew.filter((c) => !c.available).length > 0 ? 'ops-stat-warn' : ''}`}>
          <XCircle size={16} /> <strong>{crew.filter((c) => !c.available).length}</strong> <span>Unavailable Crew</span>
        </div>
        <div className={`ops-stat ${gates.some((g) => g.conflict) ? 'ops-stat-danger' : ''}`}>
          <AlertTriangle size={16} /> <strong>{gates.filter((g) => g.conflict).length}</strong> <span>Gate Conflicts</span>
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

      <section className="ops-section">
        {activeTab === 'aircraft' && (
          <AircraftTab
            aircraft={aircraft}
            loading={loading.aircraft}
            onDelete={handleDeleteAircraft}
            onRefresh={fetchAircraft}
          />
        )}
        {activeTab === 'crew' && (
          <CrewTab
            crew={crew}
            loading={loading.crew}
            onDelete={handleDeleteCrew}
            onRefresh={fetchCrew}
          />
        )}
        {activeTab === 'roster' && (
          <RosterTab loading={false} onRefresh={() => {}} />
        )}
        {activeTab === 'gates' && (
          <GatesTab gates={gates} loading={loading.gates} onRefresh={fetchGates} />
        )}
      </section>
    </Layout>
  );
}
