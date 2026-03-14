import apiClient from '../api/apiClient';

const BASE = '/workflows';

export const workflowService = {
  list: () => apiClient.get(BASE),
  get: (id) => apiClient.get(`${BASE}/${id}`),
  audit: (id) => apiClient.get(`${BASE}/${id}/audit`),
  create: (payload) => apiClient.post(BASE, payload),
  submit: (id, payload) => apiClient.post(`${BASE}/${id}/submit`, payload),
  approve: (id, payload) => apiClient.post(`${BASE}/${id}/approve`, payload),
  execute: (id, payload) => apiClient.post(`${BASE}/${id}/execute`, payload),
  markAudited: (id, payload) => apiClient.post(`${BASE}/${id}/audit`, payload),
};
