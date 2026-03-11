package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.PassengerDTO;
import com.example.flight_management_system.service.PassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping
    public ResponseEntity<List<PassengerDTO>> getAll() {
        return ResponseEntity.ok(passengerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PassengerDTO> create(@RequestBody PassengerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerDTO> update(@PathVariable Long id, @RequestBody PassengerDTO dto) {
        return ResponseEntity.ok(passengerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
