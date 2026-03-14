package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.AircraftStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AircraftDTO {
    private Long id;

    @NotBlank(message = "Registration is required")
    private String registration;

    private String model;
    private String manufacturer;
    private int totalSeats;
    private int economySeats;
    private int businessSeats;

    @NotNull(message = "Status is required")
    private AircraftStatus status;

    private LocalDateTime nextMaintenanceAt;
    private int totalFlightHours;
    private String airlineCode;

    // Computed – current assignment
    private Long currentFlightId;
    private String currentFlightRoute;
}
