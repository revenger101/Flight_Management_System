package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.WorkflowAuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowAuditTrailRepository extends JpaRepository<WorkflowAuditTrail, Long> {
    List<WorkflowAuditTrail> findByWorkflowIdOrderByChangedAtDesc(Long workflowId);
}
