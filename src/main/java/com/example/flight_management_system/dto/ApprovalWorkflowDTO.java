package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.entity.enums.WorkflowState;
import com.example.flight_management_system.entity.enums.WorkflowType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalWorkflowDTO {
    private Long id;
    private WorkflowType workflowType;
    private WorkflowState state;
    private UserRole requiredApprovalRole;
    private String targetEntity;
    private Long targetEntityId;
    private String title;
    private String description;
    private String oldValues;
    private String newValues;
    private String createdBy;
    private LocalDateTime createdAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String executedBy;
    private LocalDateTime executedAt;
    private String auditedBy;
    private LocalDateTime auditedAt;
    private String reviewComment;
    private String executionNote;
    private String auditNote;
}
