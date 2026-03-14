package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.GateSlot;
import com.example.flight_management_system.entity.enums.GateSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GateSlotRepository extends JpaRepository<GateSlot, Long> {
    List<GateSlot> findByFlightId(Long flightId);
    List<GateSlot> findByAirportIdAndGateNumber(Long airportId, int gateNumber);
    List<GateSlot> findByStatus(GateSlotStatus status);
    List<GateSlot> findByAirportId(Long airportId);

    @Query("SELECT g FROM GateSlot g WHERE g.airport.id = :airportId AND g.gateNumber = :gate " +
           "AND g.scheduledStart < :end AND g.scheduledEnd > :start")
    List<GateSlot> findPotentialOverlaps(
            @Param("airportId") Long airportId,
            @Param("gate") int gate,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
