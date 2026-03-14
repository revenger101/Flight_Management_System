package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.WorkflowState;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowAuditTrailDTO {
    private Long id;
    private Long workflowId;
    private String action;
    private WorkflowState oldState;
    private WorkflowState newState;
    private String changedBy;
    private LocalDateTime changedAt;
    private String oldValues;
    private String newValues;
    private String note;
}
