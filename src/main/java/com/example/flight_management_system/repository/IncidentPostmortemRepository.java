package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.IncidentPostmortem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentPostmortemRepository extends JpaRepository<IncidentPostmortem, Long> {
    Optional<IncidentPostmortem> findByTicketId(Long ticketId);
}
