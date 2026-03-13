import apiClient from '../api/apiClient';

const BASE = '/bookings';

const generateIdempotencyKey = () => {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `bk-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

export const bookingService = {
  getAll: () => apiClient.get(BASE),
  getById: (id) => apiClient.get(`${BASE}/${id}`),
  getByPassenger: (id) => apiClient.get(`${BASE}/passenger/${id}`),
  getByFlight: (id) => apiClient.get(`${BASE}/flight/${id}`),
  create: (data, idempotencyKey = generateIdempotencyKey()) =>
    apiClient.post(BASE, data, {
      headers: { 'Idempotency-Key': idempotencyKey },
    }),
  update: (id, data) => apiClient.put(`${BASE}/${id}`, data),
  cancel: (id, reason) => apiClient.post(`${BASE}/${id}/cancel`, null, { params: { reason } }),
  rebook: (id, payload) => apiClient.post(`${BASE}/${id}/rebook`, payload),
  delete: (id) => apiClient.delete(`${BASE}/${id}`),
};