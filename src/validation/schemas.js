import { z } from 'zod';

export const bookingFormSchema = z.object({
  kind: z.enum(['One-way', 'Round-trip']),
  date: z.string().min(1, 'Date is required'),
  type: z.enum(['ECONOMIC', 'BUSINESS']),
  passengerId: z.string().min(1, 'Passenger is required'),
  flightId: z.string().min(1, 'Flight is required'),
});
