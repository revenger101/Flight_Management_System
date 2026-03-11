package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, Long> {
}
