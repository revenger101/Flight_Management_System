package com.example.flight_management_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPostmortemDTO {
    private Long id;
    private Long ticketId;
    private String incidentSummary;
    private String rootCause;
    private String timeline;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<IncidentCorrectiveActionDTO> correctiveActions;
}
