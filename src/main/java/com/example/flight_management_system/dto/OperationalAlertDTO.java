package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalAlertDTO {
    private Long id;
    private AlertType alertType;
    private AlertSeverity severity;
    private Long flightId;
    private String flightRoute;
    private String message;
    private String details;
    private LocalDateTime triggeredAt;
    private LocalDateTime slaDeadline;
    /** Negative value means SLA already breached. */
    private long slaRemainingSeconds;
    private boolean slaBreached;
    private LocalDateTime resolvedAt;
    private boolean resolved;
    private String resolvedBy;
    private String resolution;
}
