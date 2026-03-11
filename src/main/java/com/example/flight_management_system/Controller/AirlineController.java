package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AirlineDTO;
import com.example.flight_management_system.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    @GetMapping
    public ResponseEntity<List<AirlineDTO>> getAll() {
        return ResponseEntity.ok(airlineService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AirlineDTO> create(@RequestBody AirlineDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airlineService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineDTO> update(@PathVariable Long id, @RequestBody AirlineDTO dto) {
        return ResponseEntity.ok(airlineService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        airlineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
