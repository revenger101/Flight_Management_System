import apiClient from '../api/apiClient';

const BASE = '/operations';

export const operationsService = {
  // Live board
  getLiveBoard: () => apiClient.get(`${BASE}/live-board`),

  // Delay propagation
  propagateDelay: (flightId, additionalDelayMinutes) =>
    apiClient.post(`${BASE}/flights/${flightId}/propagate-delay`, null, {
      params: { additionalDelayMinutes },
    }),

  // Alerts
  getAlerts: () => apiClient.get(`${BASE}/alerts`),
  getAlertsByFlight: (flightId) => apiClient.get(`${BASE}/alerts/flight/${flightId}`),
  triggerAlert: (params) => apiClient.post(`${BASE}/alerts/trigger`, null, { params }),
  resolveAlert: (alertId, resolution) =>
    apiClient.post(`${BASE}/alerts/${alertId}/resolve`, null, { params: { resolution } }),

  // Disruptions
  getDisruptions: () => apiClient.get(`${BASE}/disruptions`),
  getDisruptionPlan: (flightId) => apiClient.get(`${BASE}/disruptions/${flightId}`),
  executeRebooking: (bookingId, newFlightId) =>
    apiClient.post(`${BASE}/disruptions/rebook`, null, { params: { bookingId, newFlightId } }),
};
