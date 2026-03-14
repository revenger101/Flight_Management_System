import apiClient from '../api/apiClient';

const BASE = '/pricing';
const ADMIN_BASE = '/pricing/admin';

export const pricingService = {
  quote: (payload) => apiClient.post(`${BASE}/quote`, payload),

  getFareRules: () => apiClient.get(`${ADMIN_BASE}/fare-rules`),
  createFareRule: (payload) => apiClient.post(`${ADMIN_BASE}/fare-rules`, payload),
  updateFareRule: (id, payload) => apiClient.put(`${ADMIN_BASE}/fare-rules/${id}`, payload),
  deleteFareRule: (id) => apiClient.delete(`${ADMIN_BASE}/fare-rules/${id}`),

  getCampaigns: () => apiClient.get(`${ADMIN_BASE}/campaigns`),
  createCampaign: (payload) => apiClient.post(`${ADMIN_BASE}/campaigns`, payload),
  updateCampaign: (id, payload) => apiClient.put(`${ADMIN_BASE}/campaigns/${id}`, payload),
  deleteCampaign: (id) => apiClient.delete(`${ADMIN_BASE}/campaigns/${id}`),

  getPromoCodes: () => apiClient.get(`${ADMIN_BASE}/promo-codes`),
  createPromoCode: (payload) => apiClient.post(`${ADMIN_BASE}/promo-codes`, payload),
  updatePromoCode: (id, payload) => apiClient.put(`${ADMIN_BASE}/promo-codes/${id}`, payload),
  deletePromoCode: (id) => apiClient.delete(`${ADMIN_BASE}/promo-codes/${id}`),

  getCorporateRates: () => apiClient.get(`${ADMIN_BASE}/corporate-rates`),
  createCorporateRate: (payload) => apiClient.post(`${ADMIN_BASE}/corporate-rates`, payload),
  updateCorporateRate: (id, payload) => apiClient.put(`${ADMIN_BASE}/corporate-rates/${id}`, payload),
  deleteCorporateRate: (id) => apiClient.delete(`${ADMIN_BASE}/corporate-rates/${id}`),
};
