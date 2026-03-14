package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.PricingQuoteDTO;
import com.example.flight_management_system.dto.PricingQuoteRequestDTO;
import com.example.flight_management_system.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/quote")
    public ResponseEntity<PricingQuoteDTO> quote(@Valid @RequestBody PricingQuoteRequestDTO request) {
        return ResponseEntity.ok(pricingService.quote(request));
    }
}
