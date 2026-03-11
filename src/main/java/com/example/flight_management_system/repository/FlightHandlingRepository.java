package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.FlightHandling;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightHandlingRepository extends JpaRepository<FlightHandling, Long> {
}
