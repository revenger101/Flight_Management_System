package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.BookingDTO;
import com.example.flight_management_system.dto.RebookRequestDTO;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAll() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookingDTO>> search(
            @RequestParam(required = false) Long passengerId,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(bookingService.search(passengerId, flightId, status, pageable));
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
    public ResponseEntity<BookingDTO> create(@Valid @RequestBody BookingDTO dto,
                                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(dto, idempotencyKey));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable Long id, @Valid @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(bookingService.update(id, dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancel(@PathVariable Long id,
                                             @RequestParam(value = "reason", required = false) String reason) {
        return ResponseEntity.ok(bookingService.cancel(id, reason));
    }

    @PostMapping("/{id}/rebook")
    public ResponseEntity<BookingDTO> rebook(@PathVariable Long id, @Valid @RequestBody RebookRequestDTO request) {
        return ResponseEntity.ok(bookingService.rebook(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
