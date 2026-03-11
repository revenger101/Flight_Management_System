package com.example.flight_management_system.repository;


import com.example.flight_management_system.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline , Long> {
}
