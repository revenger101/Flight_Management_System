package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.OperationalAlert;
import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalAlertRepository extends JpaRepository<OperationalAlert, Long> {
    List<OperationalAlert> findByResolvedFalseOrderByTriggeredAtDesc();
    List<OperationalAlert> findByFlightId(Long flightId);
    List<OperationalAlert> findByFlightIdAndResolvedFalse(Long flightId);
    List<OperationalAlert> findBySeverityAndResolvedFalse(AlertSeverity severity);
    boolean existsByFlightIdAndAlertTypeAndResolvedFalse(Long flightId, AlertType alertType);
}
