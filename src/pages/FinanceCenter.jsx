import { useEffect, useMemo, useState } from 'react';
import { Wallet, CreditCard, BadgeDollarSign, RefreshCw, ReceiptText, AlertOctagon, FileText, BarChart3, Sparkles, BellRing } from 'lucide-react';
import Layout from '../components/Layout';
import { financeService } from '../services/financeService';
import { notificationTemplateService } from '../services/notificationTemplateService';

export default function FinanceCenter() {
  const [payments, setPayments] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [invoice, setInvoice] = useState('');
  const [captureForm, setCaptureForm] = useState({ bookingId: '', method: 'CARD', amount: '', currency: 'USD', metadata: '' });
  const [templateForm, setTemplateForm] = useState({ eventType: 'PAYMENT_CAPTURED', channel: 'EMAIL', name: '', bodyTemplate: 'Hello {{passengerName}}, payment for booking #{{bookingId}} was captured.', active: true });

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const [pRes, dRes, tRes] = await Promise.all([
        financeService.getPayments(),
        financeService.getRevenueDashboard(),
        notificationTemplateService.getTemplates(),
      ]);
      setPayments(pRes.data || []);
      setDashboard(dRes.data);
      setTemplates(tRes.data || []);
    } catch (e) {
      setError(e?.response?.data?.message || 'Failed to load finance center');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const summary = useMemo(() => {
    if (!dashboard) return { gross: 0, refunded: 0, net: 0, ancillary: 0 };
    return {
      gross: dashboard.totalRevenue || 0,
      refunded: dashboard.totalRefunded || 0,
      net: dashboard.netRevenue || 0,
      ancillary: dashboard.ancillaryRevenue || 0,
    };
  }, [dashboard]);

  const capture = async () => {
    try {
      await financeService.capturePayment({
        bookingId: Number(captureForm.bookingId),
        method: captureForm.method,
        amount: captureForm.amount ? Number(captureForm.amount) : undefined,
        currency: captureForm.currency,
        metadata: captureForm.metadata || undefined,
      });
      setCaptureForm({ bookingId: '', method: 'CARD', amount: '', currency: 'USD', metadata: '' });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Capture failed');
    }
  };

  const refund = async (p) => {
    try {
      await financeService.refundPayment(p.id, { amount: p.amount, reason: 'Customer support refund' });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Refund failed');
    }
  };

  const chargeback = async (p) => {
    try {
      await financeService.chargebackPayment(p.id, 'Disputed payment');
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Chargeback failed');
    }
  };

  const showInvoice = async (id) => {
    try {
      const { data } = await financeService.getInvoice(id);
      setInvoice(data);
    } catch (e) {
      setError(e?.response?.data?.message || 'Invoice generation failed');
    }
  };

  const saveTemplate = async () => {
    try {
      await notificationTemplateService.upsertTemplate(templateForm);
      setTemplateForm({ eventType: 'PAYMENT_CAPTURED', channel: 'EMAIL', name: '', bodyTemplate: 'Hello {{passengerName}}, payment for booking #{{bookingId}} was captured.', active: true });
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || 'Template save failed');
    }
  };

  return (
    <Layout>
      <div className="page-header">
        <div>
          <h1 className="page-title">Payments & Financial Flows</h1>
          <p className="page-subtitle">Gateway capture, refunds, chargebacks, invoices, and revenue intelligence</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={load}><RefreshCw size={14} /> Refresh</button>
      </div>

      {error && <div className="alert-banner alert-banner-error">{error}</div>}
      {loading && <div className="ops-loading"><RefreshCw size={16} className="spin" /> Loading finance center…</div>}

      <div className="finance-hero-grid">
        <div className="finance-hero-card"><Wallet size={16} /><strong>{summary.gross}</strong><span>Gross Revenue</span></div>
        <div className="finance-hero-card"><BadgeDollarSign size={16} /><strong>{summary.net}</strong><span>Net Revenue</span></div>
        <div className="finance-hero-card"><AlertOctagon size={16} /><strong>{summary.refunded}</strong><span>Total Refunded</span></div>
        <div className="finance-hero-card"><Sparkles size={16} /><strong>{summary.ancillary}</strong><span>Ancillary Revenue</span></div>
      </div>

      <div className="finance-grid">
        <section className="ops-section">
          <div className="ops-section-header"><h2><CreditCard size={16} /> Capture Payment</h2></div>
          <div className="sched-form-grid">
            <label>Booking ID<input type="number" value={captureForm.bookingId} onChange={(e) => setCaptureForm((p) => ({ ...p, bookingId: e.target.value }))} /></label>
            <label>Method
              <select value={captureForm.method} onChange={(e) => setCaptureForm((p) => ({ ...p, method: e.target.value }))}>
                <option value="CARD">CARD</option>
                <option value="WALLET">WALLET</option>
                <option value="LOCAL_METHOD">LOCAL_METHOD</option>
              </select>
            </label>
            <label>Amount<input type="number" value={captureForm.amount} onChange={(e) => setCaptureForm((p) => ({ ...p, amount: e.target.value }))} /></label>
            <label>Currency<input value={captureForm.currency} onChange={(e) => setCaptureForm((p) => ({ ...p, currency: e.target.value }))} /></label>
            <label className="finance-span-2">Metadata<input value={captureForm.metadata} onChange={(e) => setCaptureForm((p) => ({ ...p, metadata: e.target.value }))} /></label>
          </div>
          <div className="sched-form-actions"><button className="btn btn-primary btn-sm" onClick={capture}>Capture</button></div>
        </section>

        <section className="ops-section">
          <div className="ops-section-header"><h2><ReceiptText size={16} /> Invoice</h2></div>
          <div className="finance-invoice-box">
            {invoice ? <pre>{invoice}</pre> : <p className="text-muted">Select a payment and click Invoice.</p>}
          </div>
        </section>
      </div>

      <section className="ops-section">
        <div className="ops-section-header"><h2><FileText size={16} /> Transactions</h2><span className="ops-count">{payments.length}</span></div>
        <div className="ops-board-table-wrap">
          <table className="ops-board-table">
            <thead><tr><th>ID</th><th>Booking</th><th>Method</th><th>Status</th><th>Amount</th><th>Refunded</th><th>Invoice</th><th>Actions</th></tr></thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id}>
                  <td>#{p.id}</td>
                  <td>#{p.bookingId}</td>
                  <td>{p.method}</td>
                  <td><span className={`status-badge ${p.status === 'CHARGEBACK' ? 'status-cancelled' : p.status.includes('REFUND') ? 'status-delayed' : 'status-landed'}`}>{p.status}</span></td>
                  <td>{p.amount} {p.currency}</td>
                  <td>{p.refundedAmount || 0}</td>
                  <td>{p.invoiceNumber || '—'}</td>
                  <td className="sched-actions">
                    <button className="btn btn-secondary btn-sm" onClick={() => showInvoice(p.id)}>Invoice</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => refund(p)}>Refund</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => chargeback(p)}>Chargeback</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <div className="finance-grid">
        <section className="ops-section">
          <div className="ops-section-header"><h2><BarChart3 size={16} /> Route Profitability</h2></div>
          <div className="mini-chart-list">
            {(dashboard?.routeProfitability || []).slice(0, 8).map((r) => (
              <div key={r.route} className="mini-chart-row">
                <span>{r.route}</span>
                <div className="mini-chart-track"><div className="mini-chart-fill" style={{ width: `${Math.min((r.netRevenue || 0) / Math.max(summary.net, 1) * 100, 100)}%` }} /></div>
                <strong>{r.netRevenue}</strong>
              </div>
            ))}
          </div>
        </section>

        <section className="ops-section">
          <div className="ops-section-header"><h2><BellRing size={16} /> Notification Templates</h2><span className="ops-count">{templates.length}</span></div>
          <div className="sched-form-grid">
            <label>Event Type
              <select value={templateForm.eventType} onChange={(e) => setTemplateForm((p) => ({ ...p, eventType: e.target.value }))}>
                {['PAYMENT_CAPTURED', 'REFUND_PROCESSED', 'CHARGEBACK_RECORDED', 'BOOKING_CONFIRMED', 'CHECK_IN_COMPLETED'].map((x) => <option key={x} value={x}>{x}</option>)}
              </select>
            </label>
            <label>Channel
              <select value={templateForm.channel} onChange={(e) => setTemplateForm((p) => ({ ...p, channel: e.target.value }))}>
                {['EMAIL', 'SMS', 'WHATSAPP', 'PUSH', 'WEBHOOK'].map((x) => <option key={x} value={x}>{x}</option>)}
              </select>
            </label>
            <label>Name<input value={templateForm.name} onChange={(e) => setTemplateForm((p) => ({ ...p, name: e.target.value }))} /></label>
            <label className="finance-span-2">Template
              <textarea rows={4} value={templateForm.bodyTemplate} onChange={(e) => setTemplateForm((p) => ({ ...p, bodyTemplate: e.target.value }))} />
            </label>
          </div>
          <div className="sched-form-actions"><button className="btn btn-primary btn-sm" onClick={saveTemplate}>Save Template</button></div>
          <div className="template-list">
            {templates.slice(0, 8).map((t) => (
              <div key={t.id} className="template-item">
                <strong>{t.name || t.eventType}</strong>
                <span>{t.channel}</span>
              </div>
            ))}
          </div>
        </section>
      </div>
    </Layout>
  );
}
