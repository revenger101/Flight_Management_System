package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.NotificationEventDTO;
import com.example.flight_management_system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationEventDTO>> getAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<NotificationEventDTO>> getByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(notificationService.findByFlightId(flightId));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<NotificationEventDTO>> getByPassenger(@PathVariable Long passengerId) {
        return ResponseEntity.ok(notificationService.findByPassengerId(passengerId));
    }
}
