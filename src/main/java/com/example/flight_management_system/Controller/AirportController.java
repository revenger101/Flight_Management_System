package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AirportDTO;
import com.example.flight_management_system.service.AirportService;
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
@RequestMapping("/api/airports")
@RequiredArgsConstructor
@Validated
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportDTO>> getAll() {
        return ResponseEntity.ok(airportService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AirportDTO>> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(airportService.search(country, name, airlineId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(airportService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AirportDTO> create(@Valid @RequestBody AirportDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AirportDTO> update(@PathVariable Long id, @Valid @RequestBody AirportDTO dto) {
        return ResponseEntity.ok(airportService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        airportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
