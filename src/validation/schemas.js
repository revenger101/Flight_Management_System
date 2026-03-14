import { z } from 'zod';

export const bookingFormSchema = z.object({
  kind: z.enum(['One-way', 'Round-trip']),
  date: z.string().min(1, 'Date is required'),
  type: z.enum(['ECONOMIC', 'BUSINESS']),
  passengerId: z.string().min(1, 'Passenger is required'),
  flightId: z.string().min(1, 'Flight is required'),
  baggageKg: z
    .string()
    .optional()
    .refine((value) => value === undefined || value === '' || (!Number.isNaN(Number(value)) && Number(value) >= 0), 'Baggage must be a valid non-negative number'),
  promoCode: z.string().optional(),
  corporateCode: z.string().optional(),
});
