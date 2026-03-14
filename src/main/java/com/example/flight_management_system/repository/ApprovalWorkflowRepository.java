package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.ApprovalWorkflow;
import com.example.flight_management_system.entity.enums.WorkflowState;
import com.example.flight_management_system.entity.enums.WorkflowType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Long> {
    List<ApprovalWorkflow> findByStateOrderByCreatedAtDesc(WorkflowState state);
    List<ApprovalWorkflow> findByWorkflowTypeOrderByCreatedAtDesc(WorkflowType workflowType);
}
