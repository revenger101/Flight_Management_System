package com.example.flight_management_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesAccountDTO {
    private Long id;

    @NotBlank(message = "Miles account number is required")
    @Size(max = 60, message = "Miles account number must be <= 60 characters")
    private String number;

    @Min(value = 0, message = "flightMiles must be >= 0")
    private int flightMiles;

    @Min(value = 0, message = "statusMiles must be >= 0")
    private int statusMiles;
}
