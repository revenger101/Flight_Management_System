package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.BookingDTO;
import com.example.flight_management_system.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAll() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.findById(id));
    }

    // GET /api/bookings/passenger/1
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BookingDTO>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(bookingService.findByPassengerId(passengerId));
    }

    // GET /api/bookings/flight/1
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<BookingDTO>> getByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(bookingService.findByFlightId(flightId));
    }

    @PostMapping
    public ResponseEntity<BookingDTO> create(@RequestBody BookingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable Long id, @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
