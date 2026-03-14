package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.DisruptionPlanDTO;
import com.example.flight_management_system.dto.LiveFlightBoardDTO;
import com.example.flight_management_system.dto.OperationalAlertDTO;
import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import com.example.flight_management_system.service.AlertService;
import com.example.flight_management_system.service.DisruptionService;
import com.example.flight_management_system.service.OperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;
    private final AlertService alertService;
    private final DisruptionService disruptionService;

    // ───── Live Board ─────

    @GetMapping("/live-board")
    public ResponseEntity<List<LiveFlightBoardDTO>> getLiveBoard() {
        return ResponseEntity.ok(operationsService.getLiveBoard());
    }

    @PostMapping("/flights/{flightId}/propagate-delay")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveFlightBoardDTO> propagateDelay(
            @PathVariable Long flightId,
            @RequestParam int additionalDelayMinutes) {
        return ResponseEntity.ok(operationsService.propagateDelay(flightId, additionalDelayMinutes));
    }

    // ───── Alerts ─────

    @GetMapping("/alerts")
    public ResponseEntity<List<OperationalAlertDTO>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/alerts/flight/{flightId}")
    public ResponseEntity<List<OperationalAlertDTO>> getAlertsByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(alertService.getAlertsByFlight(flightId));
    }

    @PostMapping("/alerts/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationalAlertDTO> triggerAlert(
            @RequestParam Long flightId,
            @RequestParam AlertType alertType,
            @RequestParam AlertSeverity severity,
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "") String details,
            @RequestParam(defaultValue = "30") int slaMinutes) {
        OperationalAlertDTO result = alertService.triggerAlert(
                flightId, alertType, severity, message, details, slaMinutes);
        if (result == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OperationalAlertDTO> resolveAlert(
            @PathVariable Long alertId,
            @RequestParam String resolution,
            @AuthenticationPrincipal UserDetails userDetails) {
        String by = userDetails != null ? userDetails.getUsername() : "system";
        return ResponseEntity.ok(alertService.resolveAlert(alertId, by, resolution));
    }

    // ───── Disruptions ─────

    @GetMapping("/disruptions")
    public ResponseEntity<List<DisruptionPlanDTO>> getDisruptionPlans() {
        return ResponseEntity.ok(disruptionService.getDisruptionPlans());
    }

    @GetMapping("/disruptions/{flightId}")
    public ResponseEntity<DisruptionPlanDTO> getDisruptionPlan(@PathVariable Long flightId) {
        return ResponseEntity.ok(disruptionService.getDisruptionPlan(flightId));
    }

    @PostMapping("/disruptions/rebook")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> executeRebooking(
            @RequestParam Long bookingId,
            @RequestParam Long newFlightId) {
        disruptionService.executeRebooking(bookingId, newFlightId);
        return ResponseEntity.ok().build();
    }
}
