import apiClient from '../api/apiClient';

const BASE = '/support';

export const supportService = {
  listTickets: () => apiClient.get(`${BASE}/tickets`),
  listBreached: () => apiClient.get(`${BASE}/tickets/breached`),
  getTicket: (id) => apiClient.get(`${BASE}/tickets/${id}`),
  createTicket: (payload) => apiClient.post(`${BASE}/tickets`, payload),
  updateStatus: (id, payload) => apiClient.put(`${BASE}/tickets/${id}/status`, payload),
  assign: (id, assignee) => apiClient.put(`${BASE}/tickets/${id}/assign`, null, { params: { assignee } }),
  getPostmortem: (ticketId) => apiClient.get(`${BASE}/tickets/${ticketId}/postmortem`),
  upsertPostmortem: (ticketId, payload) => apiClient.post(`${BASE}/tickets/${ticketId}/postmortem`, payload),
  addAction: (ticketId, payload) => apiClient.post(`${BASE}/tickets/${ticketId}/actions`, payload),
  updateAction: (actionId, payload) => apiClient.put(`${BASE}/actions/${actionId}`, payload),
};
