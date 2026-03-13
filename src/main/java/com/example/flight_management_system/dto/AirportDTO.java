package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportDTO {
    private Long id;

    @NotBlank(message = "Airport shortName is required")
    @Size(max = 20, message = "Airport shortName must be <= 20 characters")
    private String shortName;

    @NotBlank(message = "Airport name is required")
    @Size(max = 120, message = "Airport name must be <= 120 characters")
    private String name;

    @NotBlank(message = "Country is required")
    @Size(max = 80, message = "Country must be <= 80 characters")
    private String country;

    @DecimalMin(value = "0.0", inclusive = true, message = "Fee must be >= 0")
    private float fee;

    @NotNull(message = "airlineId is required")
    private Long airlineId;
}
