import apiClient from '../api/apiClient';

const BASE = '/tracking';

export const trackingService = {
  getLiveFlights: () => apiClient.get(`${BASE}/live`),
};
