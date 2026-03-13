package com.example.flight_management_system.dto;

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
public class RebookRequestDTO {
    @NotNull(message = "newFlightId is required")
    private Long newFlightId;
    private LocalDate newDate;
}
