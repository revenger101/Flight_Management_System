package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.PaymentMethodType;
import com.example.flight_management_system.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Double amount;

    @Column(length = 3)
    private String currency;

    @Column(length = 100)
    private String gatewayReference;

    @Column(length = 60)
    private String invoiceNumber;

    @Column(length = 1000)
    private String metadata;

    @Column(length = 500)
    private String chargebackReason;

    private Double refundedAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
