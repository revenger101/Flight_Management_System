import { useEffect, useMemo, useState } from 'react';
import { LifeBuoy, RefreshCw, AlertTriangle, Clock3, Siren, ClipboardList, PlusCircle } from 'lucide-react';
import Layout from '../components/Layout';
import { supportService } from '../services/supportService';

export default function SupportCenter() {
  const [tickets, setTickets] = useState([]);
  const [breached, setBreached] = useState([]);
  const [selectedTicketId, setSelectedTicketId] = useState('');
  const [postmortem, setPostmortem] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [ticketForm, setTicketForm] = useState({
    title: '',
    description: '',
    severity: 'MEDIUM',
    businessImpact: 3,
    assignedTo: '',
  });
  const [postmortemForm, setPostmortemForm] = useState({ incidentSummary: '', rootCause: '', timeline: '' });
  const [actionForm, setActionForm] = useState({ actionItem: '', owner: '', dueAt: '', status: 'OPEN' });

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const [tRes, bRes] = await Promise.all([
        supportService.listTickets(),
        supportService.listBreached(),
      ]);
      setTickets(tRes.data || []);
      setBreached(bRes.data || []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load support center');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stats = useMemo(() => ({
    open: tickets.filter((t) => t.status === 'OPEN' || t.status === 'IN_PROGRESS').length,
    resolved: tickets.filter((t) => t.status === 'RESOLVED' || t.status === 'CLOSED').length,
    breached: breached.length,
    p1: tickets.filter((t) => t.priority === 'P1_CRITICAL').length,
  }), [tickets, breached]);

  const createTicket = async () => {
    try {
      await supportService.createTicket({
        ...ticketForm,
        businessImpact: Number(ticketForm.businessImpact),
      });
      setTicketForm({ title: '', description: '', severity: 'MEDIUM', businessImpact: 3, assignedTo: '' });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Ticket creation failed');
    }
  };

  const updateStatus = async (ticketId, status) => {
    try {
      await supportService.updateStatus(ticketId, { status, resolutionSummary: status === 'RESOLVED' ? 'Resolved from support dashboard' : undefined });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Status update failed');
    }
  };

  const assign = async (ticketId) => {
    const assignee = prompt('Assign to (username/email):');
    if (!assignee) return;
    try {
      await supportService.assign(ticketId, assignee);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Assignment failed');
    }
  };

  const loadPostmortem = async (ticketId) => {
    setSelectedTicketId(ticketId);
    try {
      const { data } = await supportService.getPostmortem(ticketId);
      setPostmortem(data);
      setPostmortemForm({
        incidentSummary: data?.incidentSummary || '',
        rootCause: data?.rootCause || '',
        timeline: data?.timeline || '',
      });
    } catch {
      setPostmortem(null);
      setPostmortemForm({ incidentSummary: '', rootCause: '', timeline: '' });
    }
  };

  const savePostmortem = async () => {
    if (!selectedTicketId) return;
    try {
      const { data } = await supportService.upsertPostmortem(selectedTicketId, postmortemForm);
      setPostmortem(data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Postmortem save failed');
    }
  };

  const addAction = async () => {
    if (!selectedTicketId) return;
    try {
      await supportService.addAction(selectedTicketId, {
        ...actionForm,
        dueAt: actionForm.dueAt ? new Date(actionForm.dueAt).toISOString() : null,
      });
      await loadPostmortem(selectedTicketId);
      setActionForm({ actionItem: '', owner: '', dueAt: '', status: 'OPEN' });
    } catch (e) {
      setError(e?.response?.data?.message || 'Action add failed');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title"><LifeBuoy size={22} /> SLA, Incident & Support</h1>
          <p className="page-subtitle">Ticketing, auto-priority, SLA risk visibility, and postmortem corrective action control</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}><RefreshCw size={14} /> Refresh</button>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}
      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading incidents…</div>}

      <div className="support-kpis">
        <div className="support-kpi"><ClipboardList size={15} /><strong>{stats.open}</strong><span>Open</span></div>
        <div className="support-kpi"><Clock3 size={15} /><strong>{stats.breached}</strong><span>SLA Breached</span></div>
        <div className="support-kpi"><Siren size={15} /><strong>{stats.p1}</strong><span>P1 Critical</span></div>
        <div className="support-kpi"><AlertTriangle size={15} /><strong>{stats.resolved}</strong><span>Resolved/Closed</span></div>
      </div>

      <div className="support-layout">
        <section className="ops-section">
          <div className="ops-section-header"><h2><PlusCircle size={16} /> New Incident Ticket</h2></div>
          <div className="sched-form-grid">
            <label>Title<input value={ticketForm.title} onChange={(e) => setTicketForm((p) => ({ ...p, title: e.target.value }))} /></label>
            <label>Severity
              <select value={ticketForm.severity} onChange={(e) => setTicketForm((p) => ({ ...p, severity: e.target.value }))}>
                <option value="LOW">LOW</option><option value="MEDIUM">MEDIUM</option><option value="HIGH">HIGH</option><option value="CRITICAL">CRITICAL</option>
              </select>
            </label>
            <label>Business Impact (1-5)<input type="number" min={1} max={5} value={ticketForm.businessImpact} onChange={(e) => setTicketForm((p) => ({ ...p, businessImpact: e.target.value }))} /></label>
            <label>Assignee<input value={ticketForm.assignedTo} onChange={(e) => setTicketForm((p) => ({ ...p, assignedTo: e.target.value }))} /></label>
            <label className="workflow-span-2">Description<textarea rows={4} value={ticketForm.description} onChange={(e) => setTicketForm((p) => ({ ...p, description: e.target.value }))} /></label>
          </div>
          <div className="sched-form-actions"><button className="btn btn-primary btn-sm" onClick={createTicket}>Create Ticket</button></div>
        </section>

        <section className="ops-section">
          <div className="ops-section-header"><h2><ClipboardList size={16} /> Incidents</h2><span className="ops-count">{tickets.length}</span></div>
          <div className="ops-board-table-wrap">
            <table className="ops-board-table">
              <thead><tr><th>ID</th><th>Title</th><th>Severity</th><th>Priority</th><th>Status</th><th>SLA Due</th><th>Assigned</th><th>Actions</th></tr></thead>
              <tbody>
                {tickets.map((t) => (
                  <tr key={t.id} className={t.slaBreached ? 'gate-conflict-row' : ''}>
                    <td>#{t.id}</td>
                    <td>{t.title}</td>
                    <td><span className={`status-badge ${t.severity === 'CRITICAL' || t.severity === 'HIGH' ? 'status-cancelled' : t.severity === 'MEDIUM' ? 'status-delayed' : 'status-scheduled'}`}>{t.severity}</span></td>
                    <td>{t.priority}</td>
                    <td><span className={`status-badge ${t.status === 'RESOLVED' || t.status === 'CLOSED' ? 'status-landed' : 'status-boarding'}`}>{t.status}</span></td>
                    <td>{t.slaDueAt ? new Date(t.slaDueAt).toLocaleString() : '—'}</td>
                    <td>{t.assignedTo || '—'}</td>
                    <td className="sched-actions">
                      <button className="btn btn-secondary btn-sm" onClick={() => updateStatus(t.id, 'IN_PROGRESS')}>Start</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => updateStatus(t.id, 'RESOLVED')}>Resolve</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => assign(t.id)}>Assign</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => loadPostmortem(t.id)}>Postmortem</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <section className="ops-section">
        <div className="ops-section-header"><h2>Postmortem & Corrective Actions</h2><span className="ops-count">Ticket #{selectedTicketId || '—'}</span></div>
        {!selectedTicketId && <p className="text-muted">Select Postmortem on a ticket to start analysis and action tracking.</p>}
        {selectedTicketId && (
          <>
            <div className="sched-form-grid">
              <label className="workflow-span-2">Incident Summary<textarea rows={3} value={postmortemForm.incidentSummary} onChange={(e) => setPostmortemForm((p) => ({ ...p, incidentSummary: e.target.value }))} /></label>
              <label className="workflow-span-2">Root Cause<textarea rows={3} value={postmortemForm.rootCause} onChange={(e) => setPostmortemForm((p) => ({ ...p, rootCause: e.target.value }))} /></label>
              <label className="workflow-span-2">Timeline<textarea rows={3} value={postmortemForm.timeline} onChange={(e) => setPostmortemForm((p) => ({ ...p, timeline: e.target.value }))} /></label>
            </div>
            <div className="sched-form-actions"><button className="btn btn-primary btn-sm" onClick={savePostmortem}>Save Postmortem</button></div>

            <div className="corrective-panel">
              <h3>Corrective Actions</h3>
              <div className="sched-form-grid">
                <label className="workflow-span-2">Action Item<input value={actionForm.actionItem} onChange={(e) => setActionForm((p) => ({ ...p, actionItem: e.target.value }))} /></label>
                <label>Owner<input value={actionForm.owner} onChange={(e) => setActionForm((p) => ({ ...p, owner: e.target.value }))} /></label>
                <label>Due At<input type="datetime-local" value={actionForm.dueAt} onChange={(e) => setActionForm((p) => ({ ...p, dueAt: e.target.value }))} /></label>
                <label>Status
                  <select value={actionForm.status} onChange={(e) => setActionForm((p) => ({ ...p, status: e.target.value }))}>
                    <option value="OPEN">OPEN</option><option value="IN_PROGRESS">IN_PROGRESS</option><option value="BLOCKED">BLOCKED</option><option value="DONE">DONE</option>
                  </select>
                </label>
              </div>
              <div className="sched-form-actions"><button className="btn btn-secondary btn-sm" onClick={addAction}>Add Action</button></div>

              <div className="template-list">
                {(postmortem?.correctiveActions || []).map((a) => (
                  <div key={a.id} className="template-item">
                    <div>
                      <strong>{a.actionItem}</strong>
                      <div className="text-muted">Owner: {a.owner || '—'} • Due: {a.dueAt ? new Date(a.dueAt).toLocaleString() : '—'}</div>
                    </div>
                    <span className={`status-badge ${a.status === 'DONE' ? 'status-landed' : a.status === 'BLOCKED' ? 'status-cancelled' : 'status-delayed'}`}>{a.status}</span>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}
      </section>
    </Layout>
  );
}
