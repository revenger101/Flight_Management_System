import apiClient from '../api/apiClient';
import axios from 'axios';

const BASE = '/auth';

function getBackendBaseUrl() {
  const fromEnv = import.meta.env.VITE_BACKEND_URL;
  if (fromEnv) {
    return fromEnv.replace(/\/$/, '');
  }

  const apiBase = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
  return apiBase.replace(/\/api\/?$/, '');
}

export const authService = {
  register: (payload) => apiClient.post(`${BASE}/register`, payload),
  login: (payload) => apiClient.post(`${BASE}/login`, payload),
  me: () => apiClient.get(`${BASE}/me`),
  refresh: () => axios.post(`${getBackendBaseUrl()}/api/auth/refresh`, {}, { withCredentials: true }),
  logout: () => axios.post(`${getBackendBaseUrl()}/api/auth/logout`, {}, { withCredentials: true }),
  getGoogleLoginUrl: () => `${getBackendBaseUrl()}/oauth2/authorization/google`,
};
