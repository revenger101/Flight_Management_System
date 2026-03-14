package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.CreatePaymentRequestDTO;
import com.example.flight_management_system.dto.PaymentTransactionDTO;
import com.example.flight_management_system.dto.RefundActionDTO;
import com.example.flight_management_system.dto.RevenueDashboardDTO;
import com.example.flight_management_system.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentTransactionDTO>> getPayments() {
        return ResponseEntity.ok(financeService.getAllPayments());
    }

    @GetMapping("/payments/booking/{bookingId}")
    public ResponseEntity<List<PaymentTransactionDTO>> getPaymentsByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(financeService.getPaymentsByBooking(bookingId));
    }

    @PostMapping("/payments/capture")
    public ResponseEntity<PaymentTransactionDTO> capturePayment(@Valid @RequestBody CreatePaymentRequestDTO request) {
        return ResponseEntity.ok(financeService.capturePayment(request));
    }

    @PostMapping("/payments/{paymentId}/refund")
    public ResponseEntity<PaymentTransactionDTO> refund(
            @PathVariable Long paymentId,
            @RequestBody(required = false) RefundActionDTO request) {
        return ResponseEntity.ok(financeService.processRefund(paymentId, request));
    }

    @PostMapping("/payments/{paymentId}/chargeback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentTransactionDTO> chargeback(
            @PathVariable Long paymentId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(financeService.registerChargeback(paymentId, reason));
    }

    @GetMapping("/payments/{paymentId}/invoice")
    public ResponseEntity<String> invoice(@PathVariable Long paymentId) {
        return ResponseEntity.ok(financeService.generateInvoice(paymentId));
    }

    @GetMapping("/dashboard/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueDashboardDTO> dashboard() {
        return ResponseEntity.ok(financeService.revenueDashboard());
    }
}
