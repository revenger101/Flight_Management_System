package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.IncidentPriority;
import com.example.flight_management_system.entity.enums.IncidentSeverity;
import com.example.flight_management_system.entity.enums.IncidentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentTicketDTO {
    private Long id;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentPriority priority;
    private IncidentStatus status;
    private Integer businessImpact;
    private String openedBy;
    private String assignedTo;
    private LocalDateTime openedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime slaDueAt;
    private Boolean slaBreached;
    private String resolutionSummary;
}
