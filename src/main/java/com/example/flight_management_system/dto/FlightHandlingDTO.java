package com.example.flight_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightHandlingDTO {
    private Long id;

    @Min(value = 1, message = "boardingGate must be > 0")
    private int boardingGate;

    @Min(value = 0, message = "delay must be >= 0")
    private int delay;

    @NotNull(message = "Handling date is required")
    private LocalDate date;

    @NotNull(message = "Handling time is required")
    private LocalTime time;
}
