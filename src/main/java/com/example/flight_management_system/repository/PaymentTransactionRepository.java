package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.PaymentTransaction;
import com.example.flight_management_system.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    Optional<PaymentTransaction> findByInvoiceNumber(String invoiceNumber);
}
