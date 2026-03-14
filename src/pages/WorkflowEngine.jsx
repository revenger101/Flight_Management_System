import { useEffect, useMemo, useState } from 'react';
import { ShieldCheck, RefreshCw, PlusCircle, FileDiff, History, CircleCheckBig, ClipboardCheck } from 'lucide-react';
import Layout from '../components/Layout';
import { workflowService } from '../services/workflowService';

const states = ['DRAFT', 'REVIEW', 'APPROVED', 'EXECUTED', 'AUDITED'];

export default function WorkflowEngine() {
  const [rows, setRows] = useState([]);
  const [audit, setAudit] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [createForm, setCreateForm] = useState({
    workflowType: 'FARE_CHANGE',
    requiredApprovalRole: 'ADMIN',
    targetEntity: 'FareRule',
    targetEntityId: '',
    title: '',
    description: '',
    oldValues: '{"fare":100}',
    newValues: '{"fare":115}',
  });

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await workflowService.list();
      setRows(data || []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load workflows');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const stateStats = useMemo(() => {
    const map = Object.fromEntries(states.map((s) => [s, 0]));
    rows.forEach((r) => { map[r.state] = (map[r.state] || 0) + 1; });
    return map;
  }, [rows]);

  const createDraft = async () => {
    try {
      await workflowService.create({
        ...createForm,
        targetEntityId: createForm.targetEntityId ? Number(createForm.targetEntityId) : null,
      });
      setCreateForm({ workflowType: 'FARE_CHANGE', requiredApprovalRole: 'ADMIN', targetEntity: 'FareRule', targetEntityId: '', title: '', description: '', oldValues: '{"fare":100}', newValues: '{"fare":115}' });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to create workflow');
    }
  };

  const transition = async (id, action) => {
    try {
      const req = { note: `${action} via workflow console` };
      if (action === 'submit') await workflowService.submit(id, req);
      if (action === 'approve') await workflowService.approve(id, req);
      if (action === 'execute') await workflowService.execute(id, req);
      if (action === 'audit') await workflowService.markAudited(id, req);
      await load();
      if (selectedId === id) {
        const { data } = await workflowService.audit(id);
        setAudit(data || []);
      }
    } catch (e) {
      setError(e?.response?.data?.message || `Failed to ${action}`);
    }
  };

  const openAudit = async (id) => {
    setSelectedId(id);
    try {
      const { data } = await workflowService.audit(id);
      setAudit(data || []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load audit trail');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title"><ShieldCheck size={22} /> Workflow & Approval Engine</h1>
          <p className="page-subtitle">Governance controls for fare changes, cancellations, and overrides with full auditability</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}><RefreshCw size={14} /> Refresh</button>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}
      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading workflows…</div>}

      <div className="workflow-stats">
        {states.map((s) => (
          <div key={s} className="workflow-stat-card">
            <strong>{stateStats[s] || 0}</strong>
            <span>{s}</span>
          </div>
        ))}
      </div>

      <div className="workflow-layout">
        <section className="ops-section">
          <div className="ops-section-header"><h2><PlusCircle size={16} /> New Workflow</h2></div>
          <div className="sched-form-grid">
            <label>Type
              <select value={createForm.workflowType} onChange={(e) => setCreateForm((p) => ({ ...p, workflowType: e.target.value }))}>
                <option value="FARE_CHANGE">FARE_CHANGE</option>
                <option value="FLIGHT_CANCELLATION">FLIGHT_CANCELLATION</option>
                <option value="MANUAL_OVERRIDE">MANUAL_OVERRIDE</option>
              </select>
            </label>
            <label>Target Entity<input value={createForm.targetEntity} onChange={(e) => setCreateForm((p) => ({ ...p, targetEntity: e.target.value }))} /></label>
            <label>Target ID<input type="number" value={createForm.targetEntityId} onChange={(e) => setCreateForm((p) => ({ ...p, targetEntityId: e.target.value }))} /></label>
            <label>Title<input value={createForm.title} onChange={(e) => setCreateForm((p) => ({ ...p, title: e.target.value }))} /></label>
            <label className="workflow-span-2">Description<input value={createForm.description} onChange={(e) => setCreateForm((p) => ({ ...p, description: e.target.value }))} /></label>
            <label className="workflow-span-2">Old Values JSON<textarea rows={3} value={createForm.oldValues} onChange={(e) => setCreateForm((p) => ({ ...p, oldValues: e.target.value }))} /></label>
            <label className="workflow-span-2">New Values JSON<textarea rows={3} value={createForm.newValues} onChange={(e) => setCreateForm((p) => ({ ...p, newValues: e.target.value }))} /></label>
          </div>
          <div className="sched-form-actions">
            <button className="btn btn-primary btn-sm" onClick={createDraft}>Create Draft</button>
          </div>
        </section>

        <section className="ops-section">
          <div className="ops-section-header"><h2><FileDiff size={16} /> Workflow Queue</h2><span className="ops-count">{rows.length}</span></div>
          <div className="ops-board-table-wrap">
            <table className="ops-board-table">
              <thead><tr><th>ID</th><th>Type</th><th>Title</th><th>State</th><th>Created By</th><th>Approver Role</th><th>Actions</th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>#{r.id}</td>
                    <td>{r.workflowType}</td>
                    <td>{r.title || '—'}</td>
                    <td><span className={`status-badge ${r.state === 'AUDITED' ? 'status-landed' : r.state === 'APPROVED' ? 'status-boarding' : r.state === 'REVIEW' ? 'status-delayed' : 'status-scheduled'}`}>{r.state}</span></td>
                    <td>{r.createdBy || 'system'}</td>
                    <td>{r.requiredApprovalRole}</td>
                    <td className="sched-actions">
                      {r.state === 'DRAFT' && <button className="btn btn-secondary btn-sm" onClick={() => transition(r.id, 'submit')}>Submit</button>}
                      {r.state === 'REVIEW' && <button className="btn btn-secondary btn-sm" onClick={() => transition(r.id, 'approve')}>Approve</button>}
                      {r.state === 'APPROVED' && <button className="btn btn-secondary btn-sm" onClick={() => transition(r.id, 'execute')}>Execute</button>}
                      {r.state === 'EXECUTED' && <button className="btn btn-secondary btn-sm" onClick={() => transition(r.id, 'audit')}>Audit</button>}
                      <button className="btn btn-secondary btn-sm" onClick={() => openAudit(r.id)}><History size={13} /> Trail</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </div>

      <section className="ops-section">
        <div className="ops-section-header"><h2><ClipboardCheck size={16} /> Full Audit Trail</h2><span className="ops-count">Workflow #{selectedId || '—'}</span></div>
        <div className="audit-timeline">
          {audit.map((a) => (
            <div key={a.id} className="audit-row">
              <div className="audit-point"><CircleCheckBig size={13} /></div>
              <div className="audit-content">
                <div className="audit-title"><strong>{a.action}</strong><span>{a.oldState || '—'} {'->'} {a.newState || '—'}</span></div>
                <small>{a.changedBy} • {a.changedAt ? new Date(a.changedAt).toLocaleString() : '—'}</small>
                {a.note && <p>{a.note}</p>}
                <div className="audit-values"><code>OLD: {a.oldValues || '{}'}</code><code>NEW: {a.newValues || '{}'}</code></div>
              </div>
            </div>
          ))}
          {audit.length === 0 && <p className="text-muted">Select Trail on a workflow to inspect full change history.</p>}
        </div>
      </section>
    </Layout>
  );
}
