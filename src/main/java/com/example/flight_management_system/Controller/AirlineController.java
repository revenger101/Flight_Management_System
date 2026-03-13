package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AirlineDTO;
import com.example.flight_management_system.service.AirlineService;
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
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
@Validated
public class AirlineController {

    private final AirlineService airlineService;

    @GetMapping
    public ResponseEntity<List<AirlineDTO>> getAll() {
        return ResponseEntity.ok(airlineService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AirlineDTO>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(airlineService.search(q, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AirlineDTO> create(@Valid @RequestBody AirlineDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airlineService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AirlineDTO> update(@PathVariable Long id, @Valid @RequestBody AirlineDTO dto) {
        return ResponseEntity.ok(airlineService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        airlineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
