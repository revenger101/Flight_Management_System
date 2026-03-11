package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByName(String firstName);

}
