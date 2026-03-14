package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.CreatePaymentRequestDTO;
import com.example.flight_management_system.dto.PaymentTransactionDTO;
import com.example.flight_management_system.dto.RefundActionDTO;
import com.example.flight_management_system.dto.RevenueDashboardDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.PaymentTransaction;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.entity.enums.PaymentStatus;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceService {

    private final PaymentTransactionRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final NotificationService notificationService;

    public List<PaymentTransactionDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(PaymentTransaction::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .map(this::toDTO)
                .toList();
    }

    public List<PaymentTransactionDTO> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public PaymentTransactionDTO capturePayment(CreatePaymentRequestDTO request) {
        if (request == null || request.getBookingId() == null || request.getMethod() == null) {
            throw new BadRequestException("bookingId and method are required");
        }

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + request.getBookingId()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot capture payment for cancelled booking");
        }

        double amount = request.getAmount() != null ? request.getAmount() : (booking.getFinalFare() != null ? booking.getFinalFare() : 0.0);
        if (amount <= 0) {
            throw new BadRequestException("Payment amount must be > 0");
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .booking(booking)
                .method(request.getMethod())
                .status(PaymentStatus.CAPTURED)
                .amount(amount)
                .currency(request.getCurrency() != null ? request.getCurrency() : (booking.getCurrency() != null ? booking.getCurrency() : "USD"))
                .gatewayReference("GW-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .invoiceNumber(generateInvoiceNumber())
                .metadata(request.getMetadata())
                .refundedAmount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentTransaction saved = paymentRepository.save(tx);

        notificationService.notifyBookingEvent(
                booking,
                NotificationEventType.PAYMENT_CAPTURED,
                "Payment captured for booking #" + booking.getId() + " (" + saved.getAmount() + " " + saved.getCurrency() + ")",
                List.of(NotificationChannel.EMAIL, NotificationChannel.PUSH)
        );

        return toDTO(saved);
    }

    @Transactional
    public PaymentTransactionDTO processRefund(Long paymentId, RefundActionDTO action) {
        PaymentTransaction tx = getPayment(paymentId);
        if (tx.getStatus() == PaymentStatus.REFUNDED) {
            return toDTO(tx);
        }

        double requested = action != null && action.getAmount() != null ? action.getAmount() : tx.getAmount();
        if (requested <= 0) {
            throw new BadRequestException("Refund amount must be > 0");
        }
        if (requested > tx.getAmount()) {
            throw new BadRequestException("Refund amount cannot exceed captured amount");
        }

        tx.setRefundedAmount(requested);
        tx.setStatus(Double.compare(requested, tx.getAmount()) == 0
            ? PaymentStatus.REFUNDED
            : PaymentStatus.PARTIAL_REFUNDED);
        tx.setUpdatedAt(LocalDateTime.now());
        tx.setMetadata(appendMeta(tx.getMetadata(), "refundReason", action != null ? action.getReason() : null));

        PaymentTransaction saved = paymentRepository.save(tx);
        notificationService.notifyBookingEvent(
                saved.getBooking(),
                NotificationEventType.REFUND_PROCESSED,
                "Refund processed for booking #" + saved.getBooking().getId() + ": " + requested + " " + saved.getCurrency(),
                List.of(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP)
        );

        return toDTO(saved);
    }

    @Transactional
    public PaymentTransactionDTO registerChargeback(Long paymentId, String reason) {
        PaymentTransaction tx = getPayment(paymentId);
        tx.setStatus(PaymentStatus.CHARGEBACK);
        tx.setChargebackReason(reason != null ? reason : "Chargeback registered");
        tx.setUpdatedAt(LocalDateTime.now());

        PaymentTransaction saved = paymentRepository.save(tx);
        notificationService.notifyBookingEvent(
                saved.getBooking(),
                NotificationEventType.CHARGEBACK_RECORDED,
                "Chargeback recorded for booking #" + saved.getBooking().getId(),
                List.of(NotificationChannel.EMAIL)
        );
        return toDTO(saved);
    }

    public String generateInvoice(Long paymentId) {
        PaymentTransaction tx = getPayment(paymentId);
        if (tx.getInvoiceNumber() == null || tx.getInvoiceNumber().isBlank()) {
            tx.setInvoiceNumber(generateInvoiceNumber());
            tx.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(tx);
        }

        Booking booking = tx.getBooking();
        return "Invoice " + tx.getInvoiceNumber() + "\n"
                + "Booking: #" + (booking != null ? booking.getId() : "N/A") + "\n"
                + "Amount: " + tx.getAmount() + " " + tx.getCurrency() + "\n"
                + "Method: " + tx.getMethod() + "\n"
                + "Status: " + tx.getStatus() + "\n"
                + "IssuedAt: " + tx.getUpdatedAt();
    }

    public RevenueDashboardDTO revenueDashboard() {
        List<Booking> bookings = bookingRepository.findAll();
        List<PaymentTransaction> payments = paymentRepository.findAll();
        List<Flight> flights = flightRepository.findAll();

        double totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.CAPTURED || p.getStatus() == PaymentStatus.PARTIAL_REFUNDED || p.getStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0)
                .sum();

        double totalRefunded = payments.stream().mapToDouble(p -> p.getRefundedAmount() != null ? p.getRefundedAmount() : 0.0).sum();
        double ancillaryRevenue = bookings.stream().mapToDouble(b -> b.getExtraBaggageFee() != null ? b.getExtraBaggageFee() : 0.0).sum();

        Map<Long, List<Booking>> byFlight = bookings.stream()
                .filter(b -> b.getFlight() != null)
                .collect(Collectors.groupingBy(b -> b.getFlight().getId()));

        List<RevenueDashboardDTO.YieldDTO> yieldRows = flights.stream().map(f -> {
            List<Booking> flightBookings = byFlight.getOrDefault(f.getId(), List.of());
            long confirmed = flightBookings.stream().filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
            double occupancy = f.getSeatCapacity() > 0
                    ? (confirmed * 100.0 / f.getSeatCapacity()) : 0.0;
            double avgYield = flightBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                    .mapToDouble(b -> b.getFinalFare() != null ? b.getFinalFare() : 0.0)
                    .average()
                    .orElse(0.0);

            String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
            String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";

            return RevenueDashboardDTO.YieldDTO.builder()
                    .flightId(f.getId())
                    .route(dep + " -> " + arr)
                    .seatCapacity(f.getSeatCapacity())
                    .confirmedBookings(confirmed)
                    .occupancyPercent(round2(occupancy))
                    .avgYield(round2(avgYield))
                    .build();
        }).sorted(Comparator.comparing(RevenueDashboardDTO.YieldDTO::getOccupancyPercent).reversed()).toList();

        Map<String, List<PaymentTransaction>> routePayments = payments.stream()
                .filter(p -> p.getBooking() != null && p.getBooking().getFlight() != null)
                .collect(Collectors.groupingBy(p -> route(p.getBooking().getFlight())));

        Map<String, Long> routeBookingsCount = bookings.stream()
                .filter(b -> b.getFlight() != null)
                .collect(Collectors.groupingBy(b -> route(b.getFlight()), Collectors.counting()));

        List<RevenueDashboardDTO.RouteRevenueDTO> routeRows = routePayments.entrySet().stream().map(entry -> {
            String route = entry.getKey();
            List<PaymentTransaction> txs = entry.getValue();
            double gross = txs.stream().mapToDouble(p -> p.getAmount() != null ? p.getAmount() : 0.0).sum();
            double refunded = txs.stream().mapToDouble(p -> p.getRefundedAmount() != null ? p.getRefundedAmount() : 0.0).sum();
            return RevenueDashboardDTO.RouteRevenueDTO.builder()
                    .route(route)
                    .bookings(routeBookingsCount.getOrDefault(route, 0L))
                    .grossRevenue(round2(gross))
                    .refunded(round2(refunded))
                    .netRevenue(round2(gross - refunded))
                    .build();
        }).sorted(Comparator.comparing(RevenueDashboardDTO.RouteRevenueDTO::getNetRevenue).reversed()).toList();

        return RevenueDashboardDTO.builder()
                .totalRevenue(round2(totalRevenue))
                .totalRefunded(round2(totalRefunded))
                .netRevenue(round2(totalRevenue - totalRefunded))
                .ancillaryRevenue(round2(ancillaryRevenue))
                .routeProfitability(routeRows)
                .occupancyVsYield(yieldRows)
                .build();
    }

    private String route(Flight f) {
        String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
        String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";
        return dep + " -> " + arr;
    }

    private PaymentTransaction getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String appendMeta(String metadata, String key, String value) {
        if (value == null || value.isBlank()) {
            return metadata;
        }
        String safe = key + "=" + value.replace('|', '/');
        if (metadata == null || metadata.isBlank()) {
            return safe;
        }
        return metadata + "|" + safe;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private PaymentTransactionDTO toDTO(PaymentTransaction tx) {
        return PaymentTransactionDTO.builder()
                .id(tx.getId())
                .bookingId(tx.getBooking() != null ? tx.getBooking().getId() : null)
                .method(tx.getMethod())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .gatewayReference(tx.getGatewayReference())
                .invoiceNumber(tx.getInvoiceNumber())
                .metadata(tx.getMetadata())
                .chargebackReason(tx.getChargebackReason())
                .refundedAmount(tx.getRefundedAmount())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
