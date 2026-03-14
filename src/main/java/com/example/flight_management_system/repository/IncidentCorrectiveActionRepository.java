package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.IncidentCorrectiveAction;
import com.example.flight_management_system.entity.enums.CorrectiveActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentCorrectiveActionRepository extends JpaRepository<IncidentCorrectiveAction, Long> {
    List<IncidentCorrectiveAction> findByPostmortemIdOrderByDueAtAsc(Long postmortemId);
    List<IncidentCorrectiveAction> findByStatusOrderByDueAtAsc(CorrectiveActionStatus status);
}
