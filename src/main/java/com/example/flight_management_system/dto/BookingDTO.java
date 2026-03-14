package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;

    @NotBlank(message = "Booking kind is required")
    @Size(max = 40, message = "Booking kind must be <= 40 characters")
    private String kind;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    private LocalDate date;

    @NotNull(message = "Booking type is required")
    private BookingType type;
    private BookingStatus status;
    private String cancellationReason;
    private Long rebookedToFlightId;

    private Double baseFare;
    private Double finalFare;
    private String currency;
    private boolean refundable;
    private Double changeFee;
    private Integer includedBaggageKg;
    private Integer baggageKg;
    private Double extraBaggageFee;
    private String campaignName;
    private String promoCode;
    private String corporateCode;
    private String seatNumber;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String boardingPassCode;

    @NotNull(message = "passengerId is required")
    private Long passengerId;

    @NotNull(message = "flightId is required")
    private Long flightId;
}
