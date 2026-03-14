package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.PaymentMethodType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequestDTO {
    @NotNull
    private Long bookingId;
    @NotNull
    private PaymentMethodType method;
    private Double amount;
    private String currency;
    private String metadata;
}
