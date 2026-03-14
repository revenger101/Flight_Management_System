package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.DisruptionPlanDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisruptionService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<DisruptionPlanDTO> getDisruptionPlans() {
        return flightRepository.findAll().stream()
                .filter(f -> f.getStatus() == FlightStatus.CANCELLED
                        || (f.getStatus() == FlightStatus.DELAYED && f.getDelayMinutes() >= 60))
                .map(this::buildPlan)
                .toList();
    }

    @Transactional(readOnly = true)
    public DisruptionPlanDTO getDisruptionPlan(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + flightId));
        return buildPlan(flight);
    }

    @Transactional
    public void executeRebooking(Long bookingId, Long newFlightId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        Flight newFlight = flightRepository.findById(newFlightId)
                .orElseThrow(() -> new RuntimeException("Target flight not found: " + newFlightId));

        if (newFlight.getStatus() == FlightStatus.CANCELLED
                || newFlight.getStatus() == FlightStatus.DEPARTED) {
            throw new IllegalArgumentException("Target flight is not bookable (status: " + newFlight.getStatus() + ")");
        }

        booking.setStatus(BookingStatus.REBOOKED);
        booking.setRebookedToFlightId(newFlightId);
        booking.setCancellationReason("Disruption rebooking from flight #" + booking.getFlight().getId());
        bookingRepository.save(booking);
    }

    // ===== Helpers =====

    private DisruptionPlanDTO buildPlan(Flight flight) {
        List<Booking> affected = flight.getBookings() == null ? List.of() :
                flight.getBookings().stream()
                        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                                || b.getStatus() == BookingStatus.WAITLISTED)
                        .toList();

        List<Flight> alternatives = flightRepository.findAll().stream()
                .filter(f -> !f.getId().equals(flight.getId()))
                .filter(f -> f.getStatus() != FlightStatus.CANCELLED
                        && f.getStatus() != FlightStatus.DEPARTED
                        && f.getStatus() != FlightStatus.LANDED)
                .filter(f -> sameRoute(f, flight))
                .limit(5)
                .toList();

        List<DisruptionPlanDTO.AffectedBookingDTO> affectedDTOs = affected.stream()
                .map(b -> DisruptionPlanDTO.AffectedBookingDTO.builder()
                        .bookingId(b.getId())
                        .passengerId(b.getPassenger() != null ? b.getPassenger().getId() : null)
                        .passengerName(b.getPassenger() != null ? b.getPassenger().getName() : "Unknown")
                        .bookingType(b.getType() != null ? b.getType().name() : null)
                        .currentStatus(b.getStatus().name())
                        .build())
                .toList();

        List<DisruptionPlanDTO.AlternativeFlightDTO> altDTOs = alternatives.stream()
                .map(alt -> {
                    long confirmedOnAlt = alt.getBookings() == null ? 0 :
                            alt.getBookings().stream()
                                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                                            || b.getStatus() == BookingStatus.REBOOKED)
                                    .count();
                    int avail = Math.max(0, alt.getSeatCapacity() - (int) confirmedOnAlt);
                    String altNote = avail >= affected.size()
                            ? "Sufficient capacity"
                            : "Limited (" + avail + " seats)";
                    return DisruptionPlanDTO.AlternativeFlightDTO.builder()
                            .flightId(alt.getId())
                            .route(routeLabel(alt))
                            .scheduledDeparture(alt.getScheduledDeparture())
                            .delayMinutes(alt.getDelayMinutes())
                            .availableSeats(avail)
                            .status(alt.getStatus())
                            .suitabilityNote(altNote)
                            .build();
                })
                .toList();

        return DisruptionPlanDTO.builder()
                .disruptedFlightId(flight.getId())
                .disruptedFlightRoute(routeLabel(flight))
                .disruptedFlightStatus(flight.getStatus())
                .disruptedFlightDelayMinutes(flight.getDelayMinutes())
                .affectedPassengerCount(affected.size())
                .affectedBookings(affectedDTOs)
                .alternatives(altDTOs)
                .build();
    }

    private boolean sameRoute(Flight a, Flight b) {
        if (a.getDepartureAirport() == null || b.getDepartureAirport() == null) return false;
        if (a.getArrivalAirport() == null || b.getArrivalAirport() == null) return false;
        return a.getDepartureAirport().getId().equals(b.getDepartureAirport().getId())
                && a.getArrivalAirport().getId().equals(b.getArrivalAirport().getId());
    }

    private String routeLabel(Flight f) {
        String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
        String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";
        return dep + " → " + arr;
    }
}
