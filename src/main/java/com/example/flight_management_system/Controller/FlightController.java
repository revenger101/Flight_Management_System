package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.FlightDTO;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Validated
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAll() {
        return ResponseEntity.ok(flightService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FlightDTO>> search(
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) Long departureAirportId,
            @RequestParam(required = false) Long arrivalAirportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(flightService.search(status, departureAirportId, arrivalAirportId, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<FlightDTO>> getByStatus(@PathVariable FlightStatus status) {
        return ResponseEntity.ok(flightService.findByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> create(@Valid @RequestBody FlightDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> update(@PathVariable Long id, @Valid @RequestBody FlightDTO dto) {
        return ResponseEntity.ok(flightService.update(id, dto));
    }

    // Ajouter un vol connecté : POST /api/flights/1/connecting/2
    @PostMapping("/{flightId}/connecting/{connectedFlightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> addConnectingFlight(
            @PathVariable Long flightId,
            @PathVariable Long connectedFlightId) {
        return ResponseEntity.ok(flightService.addConnectingFlight(flightId, connectedFlightId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
