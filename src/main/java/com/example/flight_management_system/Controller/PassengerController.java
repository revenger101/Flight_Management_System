package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.PassengerDTO;
import com.example.flight_management_system.dto.LoyaltyLedgerDTO;
import com.example.flight_management_system.service.LoyaltyService;
import com.example.flight_management_system.service.PassengerService;
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
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
@Validated
public class PassengerController {

    private final PassengerService passengerService;
    private final LoyaltyService loyaltyService;

    @GetMapping
    public ResponseEntity<List<PassengerDTO>> getAll() {
        return ResponseEntity.ok(passengerService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PassengerDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDir).orElse(Sort.Direction.ASC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(passengerService.search(name, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(passengerService.findById(id));
    }

    @GetMapping("/{id}/loyalty-ledger")
    public ResponseEntity<List<LoyaltyLedgerDTO>> getLoyaltyLedger(@PathVariable Long id) {
        return ResponseEntity.ok(loyaltyService.findByPassengerId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PassengerDTO> create(@Valid @RequestBody PassengerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(passengerService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PassengerDTO> update(@PathVariable Long id, @Valid @RequestBody PassengerDTO dto) {
        return ResponseEntity.ok(passengerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
