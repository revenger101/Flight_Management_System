package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.LiveFlightBoardDTO;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.OperationalAlert;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.repository.CrewRosterRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.OperationalAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationsService {

    private final FlightRepository flightRepository;
    private final CrewRosterRepository crewRosterRepository;
    private final OperationalAlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<LiveFlightBoardDTO> getLiveBoard() {
        List<Flight> flights = flightRepository.findAll();
        List<LiveFlightBoardDTO> board = new ArrayList<>();

        for (Flight flight : flights) {
            LocalDateTime scheduledDep = flight.getScheduledDeparture();
            LocalDateTime estimatedDep = scheduledDep != null && flight.getDelayMinutes() > 0
                    ? scheduledDep.plusMinutes(flight.getDelayMinutes())
                    : scheduledDep;

            // Crew stats
            int crewTotal = crewRosterRepository.countByFlightId(flight.getId());
            int crewCheckedIn = crewRosterRepository.countByFlightIdAndCheckedInTrue(flight.getId());
            boolean crewReady = crewTotal > 0 && crewCheckedIn == crewTotal;

            // Booking stats
            long confirmed = flight.getBookings() == null ? 0 :
                    flight.getBookings().stream()
                            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED
                                    || b.getStatus() == BookingStatus.REBOOKED)
                            .count();
            int available = Math.max(0, flight.getSeatCapacity() - (int) confirmed);

            // Alert stats
            List<OperationalAlert> activeAlerts =
                    alertRepository.findByFlightIdAndResolvedFalse(flight.getId());
            String highestSeverity = activeAlerts.stream()
                    .map(a -> a.getSeverity().name())
                    .reduce(OperationsService::higherSeverity)
                    .orElse(null);

            // Turnaround: estimate % complete based on scheduled window
            int turnaroundMin = flight.getTurnaroundMinutes() > 0 ? flight.getTurnaroundMinutes() : 45;
            int turnaroundElapsed = 0;
            int turnaroundPct = 0;
            if (scheduledDep != null) {
                LocalDateTime turnStart = scheduledDep.minusMinutes(turnaroundMin);
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(turnStart) && now.isBefore(scheduledDep)) {
                    turnaroundElapsed = (int) java.time.Duration.between(turnStart, now).toMinutes();
                    turnaroundPct = Math.min(100, (int) ((turnaroundElapsed * 100.0) / turnaroundMin));
                } else if (now.isAfter(scheduledDep)) {
                    turnaroundElapsed = turnaroundMin;
                    turnaroundPct = 100;
                }
            }

            // Connecting flight delay cascade impact
            int affectedConnecting = (flight.getDelayMinutes() > 0 && flight.getConnectingFlights() != null)
                    ? flight.getConnectingFlights().size() : 0;

            String depCode = flight.getDepartureAirport() != null
                    ? flight.getDepartureAirport().getShortName() : "?";
            String arrCode = flight.getArrivalAirport() != null
                    ? flight.getArrivalAirport().getShortName() : "?";

            board.add(LiveFlightBoardDTO.builder()
                    .flightId(flight.getId())
                    .route(depCode + " → " + arrCode)
                    .departureCode(depCode)
                    .arrivalCode(arrCode)
                    .scheduledDeparture(scheduledDep)
                    .estimatedDeparture(estimatedDep)
                    .delayMinutes(flight.getDelayMinutes())
                    .status(flight.getStatus())
                    .gateNumber(flight.getCurrentGate())
                    .aircraftRegistration(
                            flight.getAircraft() != null ? flight.getAircraft().getRegistration() : null)
                    .aircraftModel(
                            flight.getAircraft() != null ? flight.getAircraft().getModel() : null)
                    .totalSeats(flight.getSeatCapacity())
                    .confirmedBookings((int) confirmed)
                    .seatsAvailable(available)
                    .crewAssigned(crewTotal)
                    .crewCheckedIn(crewCheckedIn)
                    .crewReady(crewReady)
                    .turnaroundMinutes(turnaroundMin)
                    .turnaroundElapsedMinutes(turnaroundElapsed)
                    .turnaroundPercentComplete(turnaroundPct)
                    .activeAlertCount(activeAlerts.size())
                    .highestAlertSeverity(highestSeverity)
                    .affectedConnectingFlights(affectedConnecting)
                    .build());
        }

        return board;
    }

    @Transactional
    public LiveFlightBoardDTO propagateDelay(Long flightId, int additionalDelayMinutes) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + flightId));

        int newDelay = Math.max(0, flight.getDelayMinutes() + additionalDelayMinutes);
        flight.setDelayMinutes(newDelay);
        if (newDelay > 0 && flight.getStatus() == FlightStatus.SCHEDULED) {
            flight.setStatus(FlightStatus.DELAYED);
        }
        flightRepository.save(flight);

        // Propagate to connecting flights (buffer of 30 min for connection time)
        if (additionalDelayMinutes > 0 && flight.getConnectingFlights() != null) {
            int cascadeDelay = Math.max(0, additionalDelayMinutes - 30);
            for (Flight connecting : flight.getConnectingFlights()) {
                if (cascadeDelay > 0) {
                    connecting.setDelayMinutes(connecting.getDelayMinutes() + cascadeDelay);
                    if (connecting.getStatus() == FlightStatus.SCHEDULED) {
                        connecting.setStatus(FlightStatus.DELAYED);
                    }
                    flightRepository.save(connecting);
                }
            }
        }

        return getLiveBoard().stream()
                .filter(b -> b.getFlightId().equals(flightId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Board entry not found after update"));
    }

    private static String higherSeverity(String a, String b) {
        List<String> order = List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
        return order.indexOf(a) >= order.indexOf(b) ? a : b;
    }
}
