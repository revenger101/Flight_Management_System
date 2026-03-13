package com.example.flight_management_system.repository;


import com.example.flight_management_system.entity.Airline;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline , Long> {
	Page<Airline> findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(String name, String shortName, Pageable pageable);
}
