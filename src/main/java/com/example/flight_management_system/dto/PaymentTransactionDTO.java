package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.PaymentMethodType;
import com.example.flight_management_system.entity.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {
    private Long id;
    private Long bookingId;
    private PaymentMethodType method;
    private PaymentStatus status;
    private Double amount;
    private String currency;
    private String gatewayReference;
    private String invoiceNumber;
    private String metadata;
    private String chargebackReason;
    private Double refundedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
