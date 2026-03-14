package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Aircraft;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.enums.FlightStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
	List<Flight> findByStatus(FlightStatus status);

	@Query("""
			select f from Flight f
			where (:status is null or f.status = :status)
			  and (:departureAirportId is null or f.departureAirport.id = :departureAirportId)
			  and (:arrivalAirportId is null or f.arrivalAirport.id = :arrivalAirportId)
			""")
	Page<Flight> search(@Param("status") FlightStatus status,
	                   @Param("departureAirportId") Long departureAirportId,
	                   @Param("arrivalAirportId") Long arrivalAirportId,
	                   Pageable pageable);

	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
	@Query("select f from Flight f where f.id = :id")
	Optional<Flight> findWithLockById(@Param("id") Long id);

	List<Flight> findByAircraft(Aircraft aircraft);
}
