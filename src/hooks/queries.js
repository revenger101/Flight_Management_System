import { useQuery } from '@tanstack/react-query';
import { airlineService } from '../services/airlineService';
import { airportService } from '../services/airportService';
import { flightService } from '../services/flightService';
import { passengerService } from '../services/passengerService';
import { bookingService } from '../services/bookingService';

export const queryKeys = {
  airlines: ['airlines'],
  airports: ['airports'],
  flights: ['flights'],
  passengers: ['passengers'],
  bookings: ['bookings'],
  dashboard: ['dashboard'],
  flightDetails: (id) => ['flight-details', id],
  passengerDetails: (id) => ['passenger-details', id],
};

const defaultQueryOptions = {
  staleTime: 45_000,
  refetchOnWindowFocus: false,
};

export function useAirlinesQuery() {
  return useQuery({
    queryKey: queryKeys.airlines,
    queryFn: async () => (await airlineService.getAll()).data,
    ...defaultQueryOptions,
  });
}

export function useAirportsQuery() {
  return useQuery({
    queryKey: queryKeys.airports,
    queryFn: async () => (await airportService.getAll()).data,
    ...defaultQueryOptions,
  });
}

export function useFlightsQuery() {
  return useQuery({
    queryKey: queryKeys.flights,
    queryFn: async () => (await flightService.getAll()).data,
    ...defaultQueryOptions,
  });
}

export function usePassengersQuery() {
  return useQuery({
    queryKey: queryKeys.passengers,
    queryFn: async () => (await passengerService.getAll()).data,
    ...defaultQueryOptions,
  });
}

export function useBookingsQuery() {
  return useQuery({
    queryKey: queryKeys.bookings,
    queryFn: async () => (await bookingService.getAll()).data,
    ...defaultQueryOptions,
  });
}

export function useDashboardQuery() {
  return useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: async () => {
      const [al, ap, fl, pa, bo] = await Promise.all([
        airlineService.getAll(),
        airportService.getAll(),
        flightService.getAll(),
        passengerService.getAll(),
        bookingService.getAll(),
      ]);

      return {
        airlines: al.data,
        airports: ap.data,
        flights: fl.data,
        passengers: pa.data,
        bookings: bo.data,
      };
    },
    ...defaultQueryOptions,
  });
}

export function useFlightDetailsQuery(id) {
  return useQuery({
    queryKey: queryKeys.flightDetails(id),
    queryFn: async () => {
      const [f, ap, bo] = await Promise.all([
        flightService.getById(id),
        airportService.getAll(),
        bookingService.getByFlight(id),
      ]);
      return {
        flight: f.data,
        airports: ap.data || [],
        bookings: bo.data || [],
      };
    },
    enabled: Boolean(id),
    ...defaultQueryOptions,
  });
}

export function usePassengerDetailsQuery(id) {
  return useQuery({
    queryKey: queryKeys.passengerDetails(id),
    queryFn: async () => {
      const [p, bo, fl] = await Promise.all([
        passengerService.getById(id),
        bookingService.getByPassenger(id),
        flightService.getAll(),
      ]);
      return {
        passenger: p.data,
        bookings: bo.data || [],
        flights: fl.data || [],
      };
    },
    enabled: Boolean(id),
    ...defaultQueryOptions,
  });
}
