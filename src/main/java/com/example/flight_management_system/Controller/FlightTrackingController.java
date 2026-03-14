package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.LiveFlightTrackDTO;
import com.example.flight_management_system.service.FlightTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class FlightTrackingController {

    private final FlightTrackingService flightTrackingService;

    @GetMapping("/live")
    public ResponseEntity<List<LiveFlightTrackDTO>> live() {
        return ResponseEntity.ok(flightTrackingService.getLiveFlights());
    }
}
