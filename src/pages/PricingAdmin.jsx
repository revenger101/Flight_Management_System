import { useEffect, useMemo, useState } from 'react';
import { DollarSign, Plus, Trash2, RotateCcw } from 'lucide-react';
import toast from 'react-hot-toast';
import Layout from '../components/Layout';
import { pricingService } from '../services/pricingService';
import { airportService } from '../services/airportService';
import { getErrorMessage } from '../utils/errorUtils';

const tabs = [
  { key: 'fareRules', label: 'Fare Rules' },
  { key: 'campaigns', label: 'Campaigns' },
  { key: 'promoCodes', label: 'Promo Codes' },
  { key: 'corporateRates', label: 'Corporate Rates' },
];

function toDatetimeLocalValue(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const pad = (n) => `${n}`.padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export default function PricingAdmin() {
  const [activeTab, setActiveTab] = useState('fareRules');
  const [loading, setLoading] = useState(true);
  const [airports, setAirports] = useState([]);
  const [fareRules, setFareRules] = useState([]);
  const [campaigns, setCampaigns] = useState([]);
  const [promoCodes, setPromoCodes] = useState([]);
  const [corporateRates, setCorporateRates] = useState([]);

  const [fareRuleForm, setFareRuleForm] = useState({
    departureAirportId: '',
    arrivalAirportId: '',
    bookingType: 'ECONOMIC',
    baseFare: '',
    baseFareMultiplier: 1,
    refundable: false,
    changeFee: 85,
    includedBaggageKg: 20,
    extraBaggageFeePerKg: 7,
    currency: 'USD',
  });

  const [campaignForm, setCampaignForm] = useState({
    name: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    active: true,
    startsAt: '',
    endsAt: '',
    departureAirportId: '',
    arrivalAirportId: '',
    bookingType: '',
  });

  const [promoForm, setPromoForm] = useState({
    code: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    minSubtotal: 100,
    maxUses: 100,
    active: true,
    startsAt: '',
    endsAt: '',
  });

  const [corporateForm, setCorporateForm] = useState({
    corporateCode: '',
    companyName: '',
    discountPercent: 10,
    active: true,
    startsAt: '',
    endsAt: '',
    departureAirportId: '',
    arrivalAirportId: '',
    bookingType: '',
  });

  const airportOptions = useMemo(() => airports.map((a) => ({ value: String(a.id), label: `${a.shortName} - ${a.name}` })), [airports]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [airportsRes, frRes, cRes, pRes, crRes] = await Promise.all([
        airportService.getAll(),
        pricingService.getFareRules(),
        pricingService.getCampaigns(),
        pricingService.getPromoCodes(),
        pricingService.getCorporateRates(),
      ]);
      setAirports(airportsRes.data || []);
      setFareRules(frRes.data || []);
      setCampaigns(cRes.data || []);
      setPromoCodes(pRes.data || []);
      setCorporateRates(crRes.data || []);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to load pricing data'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const submitFareRule = async () => {
    try {
      const payload = {
        departureAirportId: fareRuleForm.departureAirportId ? Number(fareRuleForm.departureAirportId) : null,
        arrivalAirportId: fareRuleForm.arrivalAirportId ? Number(fareRuleForm.arrivalAirportId) : null,
        bookingType: fareRuleForm.bookingType || null,
        baseFare: fareRuleForm.baseFare === '' ? null : Number(fareRuleForm.baseFare),
        baseFareMultiplier: Number(fareRuleForm.baseFareMultiplier),
        refundable: !!fareRuleForm.refundable,
        changeFee: Number(fareRuleForm.changeFee),
        includedBaggageKg: Number(fareRuleForm.includedBaggageKg),
        extraBaggageFeePerKg: Number(fareRuleForm.extraBaggageFeePerKg),
        currency: fareRuleForm.currency,
      };
      await pricingService.createFareRule(payload);
      toast.success('Fare rule created');
      setFareRuleForm({ ...fareRuleForm, baseFare: '', departureAirportId: '', arrivalAirportId: '' });
      await loadData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to create fare rule'));
    }
  };

  const submitCampaign = async () => {
    try {
      await pricingService.createCampaign({
        ...campaignForm,
        discountValue: Number(campaignForm.discountValue),
        departureAirportId: campaignForm.departureAirportId ? Number(campaignForm.departureAirportId) : null,
        arrivalAirportId: campaignForm.arrivalAirportId ? Number(campaignForm.arrivalAirportId) : null,
        bookingType: campaignForm.bookingType || null,
      });
      toast.success('Campaign created');
      await loadData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to create campaign'));
    }
  };

  const submitPromo = async () => {
    try {
      await pricingService.createPromoCode({
        ...promoForm,
        code: promoForm.code.trim().toUpperCase(),
        discountValue: Number(promoForm.discountValue),
        minSubtotal: Number(promoForm.minSubtotal),
        maxUses: Number(promoForm.maxUses),
        usedCount: 0,
      });
      toast.success('Promo code created');
      await loadData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to create promo code'));
    }
  };

  const submitCorporate = async () => {
    try {
      await pricingService.createCorporateRate({
        ...corporateForm,
        corporateCode: corporateForm.corporateCode.trim().toUpperCase(),
        discountPercent: Number(corporateForm.discountPercent),
        departureAirportId: corporateForm.departureAirportId ? Number(corporateForm.departureAirportId) : null,
        arrivalAirportId: corporateForm.arrivalAirportId ? Number(corporateForm.arrivalAirportId) : null,
        bookingType: corporateForm.bookingType || null,
      });
      toast.success('Corporate rate created');
      await loadData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to create corporate rate'));
    }
  };

  const removeItem = async (kind, id) => {
    try {
      if (kind === 'fareRules') await pricingService.deleteFareRule(id);
      if (kind === 'campaigns') await pricingService.deleteCampaign(id);
      if (kind === 'promoCodes') await pricingService.deletePromoCode(id);
      if (kind === 'corporateRates') await pricingService.deleteCorporateRate(id);
      toast.success('Deleted');
      await loadData();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Delete failed'));
    }
  };

  return (
    <Layout title="Pricing Engine" subtitle="Revenue management, discounts, and policy controls">
      <div className="page-header">
        <div>
          <h1 className="page-title">Pricing Configuration</h1>
          <p className="page-subtitle">Manage fare rules, campaigns, promo codes, and corporate rates.</p>
        </div>
        <button className="btn btn-secondary" onClick={loadData}><RotateCcw size={14} /> Refresh</button>
      </div>

      <div className="pricing-tabs">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            className={`btn ${activeTab === tab.key ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="table-container"><p>Loading pricing data...</p></div>
      ) : (
        <>
          {activeTab === 'fareRules' && (
            <>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Create Fare Rule</span></div>
                <div className="filters-grid">
                  <select className="form-select" value={fareRuleForm.departureAirportId} onChange={(e) => setFareRuleForm({ ...fareRuleForm, departureAirportId: e.target.value })}>
                    <option value="">Any departure</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={fareRuleForm.arrivalAirportId} onChange={(e) => setFareRuleForm({ ...fareRuleForm, arrivalAirportId: e.target.value })}>
                    <option value="">Any arrival</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={fareRuleForm.bookingType} onChange={(e) => setFareRuleForm({ ...fareRuleForm, bookingType: e.target.value })}>
                    <option value="ECONOMIC">ECONOMIC</option>
                    <option value="BUSINESS">BUSINESS</option>
                  </select>
                  <input className="form-input" placeholder="Base fare (optional)" value={fareRuleForm.baseFare} onChange={(e) => setFareRuleForm({ ...fareRuleForm, baseFare: e.target.value })} />
                  <input className="form-input" type="number" step="0.01" placeholder="Base multiplier" value={fareRuleForm.baseFareMultiplier} onChange={(e) => setFareRuleForm({ ...fareRuleForm, baseFareMultiplier: e.target.value })} />
                  <input className="form-input" type="number" step="0.01" placeholder="Change fee" value={fareRuleForm.changeFee} onChange={(e) => setFareRuleForm({ ...fareRuleForm, changeFee: e.target.value })} />
                  <input className="form-input" type="number" placeholder="Included baggage kg" value={fareRuleForm.includedBaggageKg} onChange={(e) => setFareRuleForm({ ...fareRuleForm, includedBaggageKg: e.target.value })} />
                  <input className="form-input" type="number" step="0.01" placeholder="Extra baggage fee/kg" value={fareRuleForm.extraBaggageFeePerKg} onChange={(e) => setFareRuleForm({ ...fareRuleForm, extraBaggageFeePerKg: e.target.value })} />
                  <input className="form-input" placeholder="Currency" value={fareRuleForm.currency} onChange={(e) => setFareRuleForm({ ...fareRuleForm, currency: e.target.value.toUpperCase() })} />
                  <label className="checkbox-inline"><input type="checkbox" checked={fareRuleForm.refundable} onChange={(e) => setFareRuleForm({ ...fareRuleForm, refundable: e.target.checked })} /> Refundable</label>
                  <button className="btn btn-primary" onClick={submitFareRule}><Plus size={14} /> Add Rule</button>
                </div>
              </div>

              <div className="table-container">
                <div className="table-header"><span className="table-title">Fare Rules</span></div>
                <table>
                  <thead><tr><th>ID</th><th>Route</th><th>Type</th><th>Base Fare</th><th>Refundable</th><th>Baggage</th><th>Actions</th></tr></thead>
                  <tbody>
                    {fareRules.map((r) => (
                      <tr key={r.id}>
                        <td>#{r.id}</td>
                        <td>{r.departureAirportId || '*'} → {r.arrivalAirportId || '*'}</td>
                        <td>{r.bookingType || 'ANY'}</td>
                        <td>{r.baseFare ?? '-'} {r.currency}</td>
                        <td>{r.refundable ? 'Yes' : 'No'}</td>
                        <td>{r.includedBaggageKg}kg (+{r.extraBaggageFeePerKg}/kg)</td>
                        <td><button className="btn btn-danger btn-sm" onClick={() => removeItem('fareRules', r.id)}><Trash2 size={13} /></button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {activeTab === 'campaigns' && (
            <>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Create Campaign</span></div>
                <div className="filters-grid">
                  <input className="form-input" placeholder="Name" value={campaignForm.name} onChange={(e) => setCampaignForm({ ...campaignForm, name: e.target.value })} />
                  <input className="form-input" placeholder="Description" value={campaignForm.description} onChange={(e) => setCampaignForm({ ...campaignForm, description: e.target.value })} />
                  <select className="form-select" value={campaignForm.discountType} onChange={(e) => setCampaignForm({ ...campaignForm, discountType: e.target.value })}>
                    <option value="PERCENTAGE">PERCENTAGE</option>
                    <option value="FIXED">FIXED</option>
                  </select>
                  <input className="form-input" type="number" step="0.01" placeholder="Discount value" value={campaignForm.discountValue} onChange={(e) => setCampaignForm({ ...campaignForm, discountValue: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={campaignForm.startsAt} onChange={(e) => setCampaignForm({ ...campaignForm, startsAt: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={campaignForm.endsAt} onChange={(e) => setCampaignForm({ ...campaignForm, endsAt: e.target.value })} />
                  <select className="form-select" value={campaignForm.departureAirportId} onChange={(e) => setCampaignForm({ ...campaignForm, departureAirportId: e.target.value })}>
                    <option value="">Any departure</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={campaignForm.arrivalAirportId} onChange={(e) => setCampaignForm({ ...campaignForm, arrivalAirportId: e.target.value })}>
                    <option value="">Any arrival</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={campaignForm.bookingType} onChange={(e) => setCampaignForm({ ...campaignForm, bookingType: e.target.value })}>
                    <option value="">ANY class</option>
                    <option value="ECONOMIC">ECONOMIC</option>
                    <option value="BUSINESS">BUSINESS</option>
                  </select>
                  <label className="checkbox-inline"><input type="checkbox" checked={campaignForm.active} onChange={(e) => setCampaignForm({ ...campaignForm, active: e.target.checked })} /> Active</label>
                  <button className="btn btn-primary" onClick={submitCampaign}><Plus size={14} /> Add Campaign</button>
                </div>
              </div>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Campaigns</span></div>
                <table>
                  <thead><tr><th>ID</th><th>Name</th><th>Discount</th><th>Window</th><th>Route</th><th>Actions</th></tr></thead>
                  <tbody>
                    {campaigns.map((c) => (
                      <tr key={c.id}>
                        <td>#{c.id}</td><td>{c.name}</td><td>{c.discountType} {c.discountValue}</td>
                        <td>{toDatetimeLocalValue(c.startsAt)} → {toDatetimeLocalValue(c.endsAt)}</td>
                        <td>{c.departureAirportId || '*'} → {c.arrivalAirportId || '*'}</td>
                        <td><button className="btn btn-danger btn-sm" onClick={() => removeItem('campaigns', c.id)}><Trash2 size={13} /></button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {activeTab === 'promoCodes' && (
            <>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Create Promo Code</span></div>
                <div className="filters-grid">
                  <input className="form-input" placeholder="Code" value={promoForm.code} onChange={(e) => setPromoForm({ ...promoForm, code: e.target.value.toUpperCase() })} />
                  <input className="form-input" placeholder="Description" value={promoForm.description} onChange={(e) => setPromoForm({ ...promoForm, description: e.target.value })} />
                  <select className="form-select" value={promoForm.discountType} onChange={(e) => setPromoForm({ ...promoForm, discountType: e.target.value })}>
                    <option value="PERCENTAGE">PERCENTAGE</option>
                    <option value="FIXED">FIXED</option>
                  </select>
                  <input className="form-input" type="number" step="0.01" placeholder="Discount value" value={promoForm.discountValue} onChange={(e) => setPromoForm({ ...promoForm, discountValue: e.target.value })} />
                  <input className="form-input" type="number" step="0.01" placeholder="Min subtotal" value={promoForm.minSubtotal} onChange={(e) => setPromoForm({ ...promoForm, minSubtotal: e.target.value })} />
                  <input className="form-input" type="number" placeholder="Max uses" value={promoForm.maxUses} onChange={(e) => setPromoForm({ ...promoForm, maxUses: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={promoForm.startsAt} onChange={(e) => setPromoForm({ ...promoForm, startsAt: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={promoForm.endsAt} onChange={(e) => setPromoForm({ ...promoForm, endsAt: e.target.value })} />
                  <label className="checkbox-inline"><input type="checkbox" checked={promoForm.active} onChange={(e) => setPromoForm({ ...promoForm, active: e.target.checked })} /> Active</label>
                  <button className="btn btn-primary" onClick={submitPromo}><Plus size={14} /> Add Promo</button>
                </div>
              </div>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Promo Codes</span></div>
                <table>
                  <thead><tr><th>Code</th><th>Discount</th><th>Usage</th><th>Min Subtotal</th><th>Actions</th></tr></thead>
                  <tbody>
                    {promoCodes.map((p) => (
                      <tr key={p.id}>
                        <td><span className="badge badge-blue">{p.code}</span></td>
                        <td>{p.discountType} {p.discountValue}</td>
                        <td>{p.usedCount ?? 0}/{p.maxUses}</td>
                        <td>{p.minSubtotal}</td>
                        <td><button className="btn btn-danger btn-sm" onClick={() => removeItem('promoCodes', p.id)}><Trash2 size={13} /></button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {activeTab === 'corporateRates' && (
            <>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Create Corporate Rate</span></div>
                <div className="filters-grid">
                  <input className="form-input" placeholder="Corporate Code" value={corporateForm.corporateCode} onChange={(e) => setCorporateForm({ ...corporateForm, corporateCode: e.target.value.toUpperCase() })} />
                  <input className="form-input" placeholder="Company Name" value={corporateForm.companyName} onChange={(e) => setCorporateForm({ ...corporateForm, companyName: e.target.value })} />
                  <input className="form-input" type="number" step="0.01" placeholder="Discount %" value={corporateForm.discountPercent} onChange={(e) => setCorporateForm({ ...corporateForm, discountPercent: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={corporateForm.startsAt} onChange={(e) => setCorporateForm({ ...corporateForm, startsAt: e.target.value })} />
                  <input className="form-input" type="datetime-local" value={corporateForm.endsAt} onChange={(e) => setCorporateForm({ ...corporateForm, endsAt: e.target.value })} />
                  <select className="form-select" value={corporateForm.departureAirportId} onChange={(e) => setCorporateForm({ ...corporateForm, departureAirportId: e.target.value })}>
                    <option value="">Any departure</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={corporateForm.arrivalAirportId} onChange={(e) => setCorporateForm({ ...corporateForm, arrivalAirportId: e.target.value })}>
                    <option value="">Any arrival</option>
                    {airportOptions.map((a) => <option key={a.value} value={a.value}>{a.label}</option>)}
                  </select>
                  <select className="form-select" value={corporateForm.bookingType} onChange={(e) => setCorporateForm({ ...corporateForm, bookingType: e.target.value })}>
                    <option value="">ANY class</option>
                    <option value="ECONOMIC">ECONOMIC</option>
                    <option value="BUSINESS">BUSINESS</option>
                  </select>
                  <label className="checkbox-inline"><input type="checkbox" checked={corporateForm.active} onChange={(e) => setCorporateForm({ ...corporateForm, active: e.target.checked })} /> Active</label>
                  <button className="btn btn-primary" onClick={submitCorporate}><Plus size={14} /> Add Corporate Rate</button>
                </div>
              </div>
              <div className="table-container">
                <div className="table-header"><span className="table-title">Corporate Rates</span></div>
                <table>
                  <thead><tr><th>Code</th><th>Company</th><th>Discount %</th><th>Route Scope</th><th>Actions</th></tr></thead>
                  <tbody>
                    {corporateRates.map((r) => (
                      <tr key={r.id}>
                        <td><span className="badge badge-gold">{r.corporateCode}</span></td>
                        <td>{r.companyName}</td>
                        <td>{r.discountPercent}%</td>
                        <td>{r.departureAirportId || '*'} → {r.arrivalAirportId || '*'}</td>
                        <td><button className="btn btn-danger btn-sm" onClick={() => removeItem('corporateRates', r.id)}><Trash2 size={13} /></button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </>
      )}

      <div className="stats-grid" style={{ marginTop: 18, gridTemplateColumns: 'repeat(4, minmax(150px, 1fr))' }}>
        <div className="stat-card">
          <div className="stat-icon"><DollarSign size={16} /></div>
          <div className="stat-content"><h3>{fareRules.length}</h3><p>Fare Rules</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon"><DollarSign size={16} /></div>
          <div className="stat-content"><h3>{campaigns.length}</h3><p>Campaigns</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon"><DollarSign size={16} /></div>
          <div className="stat-content"><h3>{promoCodes.length}</h3><p>Promo Codes</p></div>
        </div>
        <div className="stat-card">
          <div className="stat-icon"><DollarSign size={16} /></div>
          <div className="stat-content"><h3>{corporateRates.length}</h3><p>Corporate Rates</p></div>
        </div>
      </div>
    </Layout>
  );
}
