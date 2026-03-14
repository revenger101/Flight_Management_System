package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.WorkflowState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private ApprovalWorkflow workflow;

    @Column(length = 80)
    private String action;

    @Enumerated(EnumType.STRING)
    private WorkflowState oldState;

    @Enumerated(EnumType.STRING)
    private WorkflowState newState;

    @Column(length = 120)
    private String changedBy;

    private LocalDateTime changedAt;

    @Column(length = 3000)
    private String oldValues;

    @Column(length = 3000)
    private String newValues;

    @Column(length = 1200)
    private String note;
}
