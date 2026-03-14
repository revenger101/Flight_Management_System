package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.FareRule;
import com.example.flight_management_system.entity.enums.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    Optional<FareRule> findFirstByDepartureAirportIdAndArrivalAirportIdAndBookingType(Long departureAirportId, Long arrivalAirportId, BookingType bookingType);
    Optional<FareRule> findFirstByDepartureAirportIdAndArrivalAirportIdAndBookingTypeIsNull(Long departureAirportId, Long arrivalAirportId);
}
