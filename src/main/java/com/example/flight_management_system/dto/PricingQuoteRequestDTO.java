package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.BookingType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingQuoteRequestDTO {

    @NotNull(message = "flightId is required")
    private Long flightId;

    @NotNull(message = "travelDate is required")
    @FutureOrPresent(message = "travelDate must be in the present or future")
    private LocalDate travelDate;

    @NotNull(message = "bookingType is required")
    private BookingType bookingType;

    @Min(value = 0, message = "baggageKg cannot be negative")
    private Integer baggageKg;

    private String promoCode;
    private String corporateCode;
}
