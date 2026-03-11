package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.FlightDTO;
import com.example.flight_management_system.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAll() {
        return ResponseEntity.ok(flightService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FlightDTO> create(@RequestBody FlightDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightDTO> update(@PathVariable Long id, @RequestBody FlightDTO dto) {
        return ResponseEntity.ok(flightService.update(id, dto));
    }

    // Ajouter un vol connecté : POST /api/flights/1/connecting/2
    @PostMapping("/{flightId}/connecting/{connectedFlightId}")
    public ResponseEntity<FlightDTO> addConnectingFlight(
            @PathVariable Long flightId,
            @PathVariable Long connectedFlightId) {
        return ResponseEntity.ok(flightService.addConnectingFlight(flightId, connectedFlightId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
