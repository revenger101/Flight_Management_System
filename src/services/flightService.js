import apiClient from '../api/apiClient';

const BASE = '/flights';
export const flightService = {
  getAll: () => apiClient.get(BASE),
  getById: (id) => apiClient.get(`${BASE}/${id}`),
  create: (data) => apiClient.post(BASE, data),
  update: (id, data) => apiClient.put(`${BASE}/${id}`, data),
  delete: (id) => apiClient.delete(`${BASE}/${id}`),
  addConnecting: (id, connectedId) => apiClient.post(`${BASE}/${id}/connecting/${connectedId}`),
};