package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.FlightStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDTO {
    private Long id;

    @NotNull(message = "Flight time is required")
    private LocalTime time;

    @Min(value = 1, message = "Miles must be > 0")
    private int miles;

    @Min(value = 1, message = "seatCapacity must be > 0")
    private int seatCapacity;

    @Min(value = 0, message = "overbookingLimit must be >= 0")
    private int overbookingLimit;
    private boolean waitlistEnabled;
    private Integer currentGate;

    @Min(value = 0, message = "delayMinutes must be >= 0")
    private int delayMinutes;
    private FlightStatus status;
    private int confirmedBookings;
    private int availableSeats;

    @NotNull(message = "departureAirportId is required")
    private Long departureAirportId;

    @NotNull(message = "arrivalAirportId is required")
    private Long arrivalAirportId;

    @Valid
    private List<FlightHandlingDTO> flightHandlings;
    private List<Long> connectingFlightIds;
}
