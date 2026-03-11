package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.BookingDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;

    private BookingDTO toDTO(Booking b) {
        return BookingDTO.builder()
                .id(b.getId())
                .kind(b.getKind())
                .date(b.getDate())
                .type(b.getType())
                .passengerId(b.getPassenger() != null ? b.getPassenger().getId() : null)
                .flightId(b.getFlight() != null ? b.getFlight().getId() : null)
                .build();
    }

    public List<BookingDTO> findAll() {
        return bookingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BookingDTO findById(Long id) {
        return toDTO(bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id)));
    }

    // Récupérer tous les bookings d'un passenger
    public List<BookingDTO> findByPassengerId(Long passengerId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getPassenger() != null && b.getPassenger().getId().equals(passengerId))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Récupérer tous les bookings d'un flight
    public List<BookingDTO> findByFlightId(Long flightId) {
        return bookingRepository.findAll().stream()
                .filter(b -> b.getFlight() != null && b.getFlight().getId().equals(flightId))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BookingDTO create(BookingDTO dto) {
        Passenger passenger = passengerRepository.findById(dto.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + dto.getPassengerId()));
        Flight flight = flightRepository.findById(dto.getFlightId())
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + dto.getFlightId()));

        Booking booking = Booking.builder()
                .kind(dto.getKind())
                .date(dto.getDate())
                .type(dto.getType())
                .passenger(passenger)
                .flight(flight)
                .build();
        return toDTO(bookingRepository.save(booking));
    }

    public BookingDTO update(Long id, BookingDTO dto) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        existing.setKind(dto.getKind());
        existing.setDate(dto.getDate());
        existing.setType(dto.getType());
        return toDTO(bookingRepository.save(existing));
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }
}
