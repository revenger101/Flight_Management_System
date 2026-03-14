import apiClient from '../api/apiClient';

const BASE = '/scheduling';

export const schedulingService = {
  // Aircraft
  getAircraft: () => apiClient.get(`${BASE}/aircraft`),
  getAircraftById: (id) => apiClient.get(`${BASE}/aircraft/${id}`),
  createAircraft: (data) => apiClient.post(`${BASE}/aircraft`, data),
  updateAircraft: (id, data) => apiClient.put(`${BASE}/aircraft/${id}`, data),
  deleteAircraft: (id) => apiClient.delete(`${BASE}/aircraft/${id}`),
  assignAircraftToFlight: (aircraftId, flightId) =>
    apiClient.post(`${BASE}/aircraft/${aircraftId}/assign/${flightId}`),
  unassignAircraftFromFlight: (flightId) =>
    apiClient.delete(`${BASE}/aircraft/unassign/${flightId}`),

  // Crew members
  getCrew: () => apiClient.get(`${BASE}/crew`),
  getCrewById: (id) => apiClient.get(`${BASE}/crew/${id}`),
  createCrewMember: (data) => apiClient.post(`${BASE}/crew`, data),
  updateCrewMember: (id, data) => apiClient.put(`${BASE}/crew/${id}`, data),
  deleteCrewMember: (id) => apiClient.delete(`${BASE}/crew/${id}`),

  // Roster
  getRosterByFlight: (flightId) => apiClient.get(`${BASE}/roster/flight/${flightId}`),
  getRosterByCrewMember: (crewMemberId) =>
    apiClient.get(`${BASE}/roster/crew/${crewMemberId}`),
  assignCrewToFlight: (data) => apiClient.post(`${BASE}/roster`, data),
  checkInCrew: (rosterId) => apiClient.post(`${BASE}/roster/${rosterId}/check-in`),
  removeCrewFromFlight: (rosterId) => apiClient.delete(`${BASE}/roster/${rosterId}`),

  // Gates
  getGateSlots: () => apiClient.get(`${BASE}/gates`),
  getGateSlotsByFlight: (flightId) => apiClient.get(`${BASE}/gates/flight/${flightId}`),
  getGateSlotsByAirport: (airportId) =>
    apiClient.get(`${BASE}/gates/airport/${airportId}`),
  createGateSlot: (data) => apiClient.post(`${BASE}/gates`, data),
  updateGateSlot: (id, data) => apiClient.put(`${BASE}/gates/${id}`, data),
  deleteGateSlot: (id) => apiClient.delete(`${BASE}/gates/${id}`),
  detectConflicts: (airportId) =>
    apiClient.post(`${BASE}/gates/detect-conflicts/${airportId}`),
};
