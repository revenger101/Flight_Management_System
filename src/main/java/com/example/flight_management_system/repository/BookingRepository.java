package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPassengerId(Long passengerId);
    List<Booking> findByFlightId(Long flightId);
    long countByFlightIdAndStatusIn(Long flightId, List<BookingStatus> statuses);
    boolean existsByPassengerIdAndDateAndStatusIn(Long passengerId, java.time.LocalDate date, List<BookingStatus> statuses);
    Optional<Booking> findFirstByFlightIdAndStatusOrderByIdAsc(Long flightId, BookingStatus status);
        Optional<Booking> findByIdempotencyKey(String idempotencyKey);

        @Query("""
                        select b from Booking b
                        where (:passengerId is null or b.passenger.id = :passengerId)
                            and (:flightId is null or b.flight.id = :flightId)
                            and (:status is null or b.status = :status)
                        """)
        Page<Booking> search(@Param("passengerId") Long passengerId,
                                                 @Param("flightId") Long flightId,
                                                 @Param("status") BookingStatus status,
                                                 Pageable pageable);
}
