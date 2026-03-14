import apiClient from '../api/apiClient';

const BASE = '/passenger-portal';

export const passengerPortalService = {
  getBookingsByPassenger: (passengerId) => apiClient.get(`${BASE}/passenger/${passengerId}/bookings`),
  selectSeat: (bookingId, seatNumber) =>
    apiClient.post(`${BASE}/bookings/${bookingId}/seat`, null, { params: { seatNumber } }),
  checkIn: (bookingId) => apiClient.post(`${BASE}/bookings/${bookingId}/check-in`),
  getBoardingPass: (bookingId) => apiClient.get(`${BASE}/bookings/${bookingId}/boarding-pass`),
  requestRefund: (bookingId, reason) =>
    apiClient.post(`${BASE}/bookings/${bookingId}/requests/refund`, null, { params: { reason } }),
  requestRebook: (bookingId, reason) =>
    apiClient.post(`${BASE}/bookings/${bookingId}/requests/rebook`, null, { params: { reason } }),
  getRequestsByPassenger: (passengerId) =>
    apiClient.get(`${BASE}/passenger/${passengerId}/requests`),
  getRequest: (requestId) => apiClient.get(`${BASE}/requests/${requestId}`),
  updateRequestStatus: (requestId, payload) => apiClient.put(`${BASE}/requests/${requestId}`, payload),
};
