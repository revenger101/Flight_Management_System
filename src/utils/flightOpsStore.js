const STORAGE_KEY = 'airport_flight_ops_v1';

const DEFAULT_OPS = {
  status: 'Scheduled',
  seatCapacity: 180,
  delayMinutes: 0,
};

export const FLIGHT_STATUSES = [
  'Scheduled',
  'Boarding',
  'Delayed',
  'Departed',
  'Landed',
  'Cancelled',
];

function safeParse(value) {
  try {
    return JSON.parse(value);
  } catch {
    return {};
  }
}

export function getFlightOpsMap() {
  if (typeof window === 'undefined') return {};
  return safeParse(window.localStorage.getItem(STORAGE_KEY) || '{}');
}

export function setFlightOpsMap(map) {
  if (typeof window === 'undefined') return;
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(map));
}

export function saveFlightOps(flightId, ops = {}) {
  const map = getFlightOpsMap();
  map[String(flightId)] = {
    ...DEFAULT_OPS,
    ...map[String(flightId)],
    ...ops,
  };
  setFlightOpsMap(map);
}

export function getFlightOps(flightId) {
  const map = getFlightOpsMap();
  return {
    ...DEFAULT_OPS,
    ...(map[String(flightId)] || {}),
  };
}

export function withOpsAndCapacity(flights = [], bookings = []) {
  const bookingCountByFlight = bookings.reduce((acc, b) => {
    const key = String(b.flightId);
    acc[key] = (acc[key] || 0) + 1;
    return acc;
  }, {});

  return flights.map((f) => {
    const ops = getFlightOps(f.id);
    const booked = bookingCountByFlight[String(f.id)] || 0;
    const seatCapacity = Number(ops.seatCapacity || 0);
    const availableSeats = Math.max(0, seatCapacity - booked);
    const occupancyPct = seatCapacity > 0 ? Math.round((booked / seatCapacity) * 100) : 0;

    return {
      ...f,
      status: ops.status,
      seatCapacity,
      delayMinutes: Number(ops.delayMinutes || 0),
      availableSeats,
      occupancyPct,
      bookedSeats: booked,
    };
  });
}

export function statusOrder(status) {
  return FLIGHT_STATUSES.indexOf(status);
}
