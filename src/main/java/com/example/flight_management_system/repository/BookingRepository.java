package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.enums.BookingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Book;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByFlightMilesLessThan(int m);
    List<Booking> findBytype(BookingType s);
}
