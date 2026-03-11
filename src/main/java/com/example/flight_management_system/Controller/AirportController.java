package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AirportDTO;
import com.example.flight_management_system.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportDTO>> getAll() {
        return ResponseEntity.ok(airportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirportDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(airportService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AirportDTO> create(@RequestBody AirportDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirportDTO> update(@PathVariable Long id, @RequestBody AirportDTO dto) {
        return ResponseEntity.ok(airportService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        airportService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
