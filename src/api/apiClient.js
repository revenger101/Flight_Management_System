import axios from 'axios';
import { normalizeApiError } from '../utils/errorUtils';
import { clearAuthToken, getAuthToken, setAuthToken } from '../utils/authStorage';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
  withCredentials: true,
});

const MAX_RETRY = 2;
let refreshPromise = null;

const shouldRetry = (status) => !status || status === 408 || status === 429 || status >= 500;

apiClient.interceptors.request.use((config) => {
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  async (err) => {
    const config = err.config || {};
    config.__retryCount = config.__retryCount || 0;

    const isRefreshRequest = config.url?.includes('/auth/refresh');
    const isAuthBootstrapRequest = config.url?.includes('/auth/login') || config.url?.includes('/auth/register');

    if (err.response?.status === 401 && !config.__authRetry && !isRefreshRequest && !isAuthBootstrapRequest) {
      config.__authRetry = true;

      try {
        if (!refreshPromise) {
          refreshPromise = axios.post(`${apiClient.defaults.baseURL.replace(/\/$/, '')}/auth/refresh`, {}, { withCredentials: true });
        }

        const { data } = await refreshPromise;
        refreshPromise = null;
        if (data?.token) {
          setAuthToken(data.token);
          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${data.token}`;
          return apiClient(config);
        }
      } catch (refreshError) {
        refreshPromise = null;
        clearAuthToken();
        return Promise.reject(normalizeApiError(refreshError));
      }
    }

    if (config.__retryCount < MAX_RETRY && shouldRetry(err.response?.status)) {
      config.__retryCount += 1;
      const delayMs = 300 * (2 ** (config.__retryCount - 1));
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      return apiClient(config);
    }

    if (err.response?.status === 401) {
      clearAuthToken();
    }

    const normalized = normalizeApiError(err);
    console.error('API Error:', normalized);
    return Promise.reject(normalized);
  }
);

export default apiClient;