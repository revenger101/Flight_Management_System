package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.BoardingPassDTO;
import com.example.flight_management_system.dto.PassengerPortalBookingDTO;
import com.example.flight_management_system.dto.ServiceRequestDTO;
import com.example.flight_management_system.dto.UpdateServiceRequestStatusDTO;
import com.example.flight_management_system.service.PassengerPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passenger-portal")
@RequiredArgsConstructor
public class PassengerPortalController {

    private final PassengerPortalService passengerPortalService;

    @GetMapping("/passenger/{passengerId}/bookings")
    public ResponseEntity<List<PassengerPortalBookingDTO>> getPassengerBookings(@PathVariable Long passengerId) {
        return ResponseEntity.ok(passengerPortalService.getPassengerBookings(passengerId));
    }

    @PostMapping("/bookings/{bookingId}/seat")
    public ResponseEntity<PassengerPortalBookingDTO> selectSeat(
            @PathVariable Long bookingId,
            @RequestParam String seatNumber) {
        return ResponseEntity.ok(passengerPortalService.selectSeat(bookingId, seatNumber));
    }

    @PostMapping("/bookings/{bookingId}/check-in")
    public ResponseEntity<BoardingPassDTO> checkIn(@PathVariable Long bookingId) {
        return ResponseEntity.ok(passengerPortalService.checkIn(bookingId));
    }

    @GetMapping("/bookings/{bookingId}/boarding-pass")
    public ResponseEntity<BoardingPassDTO> getBoardingPass(@PathVariable Long bookingId) {
        return ResponseEntity.ok(passengerPortalService.getBoardingPass(bookingId));
    }

    @PostMapping("/bookings/{bookingId}/requests/refund")
    public ResponseEntity<ServiceRequestDTO> requestRefund(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(passengerPortalService.submitRefundRequest(bookingId, reason));
    }

    @PostMapping("/bookings/{bookingId}/requests/rebook")
    public ResponseEntity<ServiceRequestDTO> requestRebook(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(passengerPortalService.submitRebookRequest(bookingId, reason));
    }

    @GetMapping("/passenger/{passengerId}/requests")
    public ResponseEntity<List<ServiceRequestDTO>> getPassengerRequests(@PathVariable Long passengerId) {
        return ResponseEntity.ok(passengerPortalService.getPassengerRequests(passengerId));
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<ServiceRequestDTO> getRequest(@PathVariable Long requestId) {
        return ResponseEntity.ok(passengerPortalService.getRequest(requestId));
    }

    @PutMapping("/requests/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceRequestDTO> updateRequest(
            @PathVariable Long requestId,
            @RequestBody UpdateServiceRequestStatusDTO dto) {
        return ResponseEntity.ok(passengerPortalService.updateRequestStatus(requestId, dto));
    }
}
