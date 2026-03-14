package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.entity.enums.WorkflowState;
import com.example.flight_management_system.entity.enums.WorkflowType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WorkflowType workflowType;

    @Enumerated(EnumType.STRING)
    private WorkflowState state;

    @Enumerated(EnumType.STRING)
    private UserRole requiredApprovalRole;

    @Column(length = 80)
    private String targetEntity;

    private Long targetEntityId;

    @Column(length = 220)
    private String title;

    @Column(length = 1200)
    private String description;

    @Column(length = 3000)
    private String oldValues;

    @Column(length = 3000)
    private String newValues;

    @Column(length = 120)
    private String createdBy;

    private LocalDateTime createdAt;

    @Column(length = 120)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 120)
    private String approvedBy;

    private LocalDateTime approvedAt;

    @Column(length = 120)
    private String executedBy;

    private LocalDateTime executedAt;

    @Column(length = 120)
    private String auditedBy;

    private LocalDateTime auditedAt;

    @Column(length = 1200)
    private String reviewComment;

    @Column(length = 1200)
    private String executionNote;

    @Column(length = 1200)
    private String auditNote;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    private List<WorkflowAuditTrail> auditTrail;
}
