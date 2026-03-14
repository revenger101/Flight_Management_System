package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.IncidentTicket;
import com.example.flight_management_system.entity.enums.IncidentPriority;
import com.example.flight_management_system.entity.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface IncidentTicketRepository extends JpaRepository<IncidentTicket, Long> {
    List<IncidentTicket> findByStatusOrderByOpenedAtDesc(IncidentStatus status);
    List<IncidentTicket> findByPriorityOrderByOpenedAtDesc(IncidentPriority priority);
    List<IncidentTicket> findBySlaDueAtBeforeAndStatusIn(LocalDateTime deadline, List<IncidentStatus> statuses);
}
