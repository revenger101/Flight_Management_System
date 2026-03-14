package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.CorrectiveActionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentCorrectiveActionDTO {
    private Long id;
    private Long postmortemId;
    private String actionItem;
    private String owner;
    private LocalDateTime dueAt;
    private CorrectiveActionStatus status;
    private LocalDateTime completedAt;
}
