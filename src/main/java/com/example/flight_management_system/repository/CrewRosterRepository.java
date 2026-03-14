package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.CrewMember;
import com.example.flight_management_system.entity.CrewRoster;
import com.example.flight_management_system.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewRosterRepository extends JpaRepository<CrewRoster, Long> {
    List<CrewRoster> findByFlight(Flight flight);
    List<CrewRoster> findByFlightId(Long flightId);
    List<CrewRoster> findByCrewMember(CrewMember crewMember);
    int countByFlightIdAndCheckedInTrue(Long flightId);
    int countByFlightId(Long flightId);
}
