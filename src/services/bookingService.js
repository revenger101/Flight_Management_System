import apiClient from '../api/apiClient';

const BASE = '/bookings';
export const bookingService = {
  getAll: () => apiClient.get(BASE),
  getById: (id) => apiClient.get(`${BASE}/${id}`),
  getByPassenger: (id) => apiClient.get(`${BASE}/passenger/${id}`),
  getByFlight: (id) => apiClient.get(`${BASE}/flight/${id}`),
  create: (data) => apiClient.post(BASE, data),
  update: (id, data) => apiClient.put(`${BASE}/${id}`, data),
  delete: (id) => apiClient.delete(`${BASE}/${id}`),
};