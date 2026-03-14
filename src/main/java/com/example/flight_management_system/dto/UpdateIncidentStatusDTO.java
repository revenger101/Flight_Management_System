package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.IncidentStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIncidentStatusDTO {
    private IncidentStatus status;
    private String resolutionSummary;
}
