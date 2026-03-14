import apiClient from '../api/apiClient';

const BASE = '/finance';

export const financeService = {
  getPayments: () => apiClient.get(`${BASE}/payments`),
  getPaymentsByBooking: (bookingId) => apiClient.get(`${BASE}/payments/booking/${bookingId}`),
  capturePayment: (payload) => apiClient.post(`${BASE}/payments/capture`, payload),
  refundPayment: (paymentId, payload) => apiClient.post(`${BASE}/payments/${paymentId}/refund`, payload),
  chargebackPayment: (paymentId, reason) =>
    apiClient.post(`${BASE}/payments/${paymentId}/chargeback`, null, { params: { reason } }),
  getInvoice: (paymentId) => apiClient.get(`${BASE}/payments/${paymentId}/invoice`),
  getRevenueDashboard: () => apiClient.get(`${BASE}/dashboard/revenue`),
};
