package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.OperationalAlertDTO;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.OperationalAlert;
import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.OperationalAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final OperationalAlertRepository alertRepository;
    private final FlightRepository flightRepository;

    public List<OperationalAlertDTO> getActiveAlerts() {
        return alertRepository.findByResolvedFalseOrderByTriggeredAtDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<OperationalAlertDTO> getAlertsByFlight(Long flightId) {
        return alertRepository.findByFlightId(flightId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public OperationalAlertDTO triggerAlert(Long flightId, AlertType type, AlertSeverity severity,
                                             String message, String details, int slaMinutes) {
        // Idempotent – don't create a duplicate open alert for the same type/flight
        if (alertRepository.existsByFlightIdAndAlertTypeAndResolvedFalse(flightId, type)) {
            return null;
        }

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + flightId));

        OperationalAlert alert = OperationalAlert.builder()
                .alertType(type)
                .severity(severity)
                .flight(flight)
                .message(message)
                .details(details)
                .triggeredAt(LocalDateTime.now())
                .slaDeadline(LocalDateTime.now().plusMinutes(slaMinutes))
                .resolved(false)
                .build();

        return toDTO(alertRepository.save(alert));
    }

    @Transactional
    public OperationalAlertDTO resolveAlert(Long alertId, String resolvedBy, String resolution) {
        OperationalAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setResolved(true);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        alert.setResolution(resolution);

        return toDTO(alertRepository.save(alert));
    }

    // ===== Helper =====

    private OperationalAlertDTO toDTO(OperationalAlert alert) {
        LocalDateTime deadline = alert.getSlaDeadline();
        long slaRemaining = deadline != null
                ? Duration.between(LocalDateTime.now(), deadline).toSeconds()
                : Long.MAX_VALUE;

        String route = null;
        if (alert.getFlight() != null) {
            Flight f = alert.getFlight();
            String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
            String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";
            route = dep + " → " + arr;
        }

        return OperationalAlertDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .flightId(alert.getFlight() != null ? alert.getFlight().getId() : null)
                .flightRoute(route)
                .message(alert.getMessage())
                .details(alert.getDetails())
                .triggeredAt(alert.getTriggeredAt())
                .slaDeadline(deadline)
                .slaRemainingSeconds(slaRemaining)
                .slaBreached(slaRemaining < 0)
                .resolvedAt(alert.getResolvedAt())
                .resolved(alert.isResolved())
                .resolvedBy(alert.getResolvedBy())
                .resolution(alert.getResolution())
                .build();
    }
}
