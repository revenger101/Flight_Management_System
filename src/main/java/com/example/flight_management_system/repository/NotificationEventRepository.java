package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findByFlightIdOrderByCreatedAtDesc(Long flightId);
    List<NotificationEvent> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
}
