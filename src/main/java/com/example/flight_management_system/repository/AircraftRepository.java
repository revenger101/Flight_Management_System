package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Aircraft;
import com.example.flight_management_system.entity.enums.AircraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistration(String registration);
    List<Aircraft> findByStatus(AircraftStatus status);
    boolean existsByRegistration(String registration);
}
