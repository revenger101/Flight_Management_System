package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirlineDTO {
    private Long id;

    @NotBlank(message = "Airline name is required")
    @Size(max = 120, message = "Airline name must be <= 120 characters")
    private String name;

    @NotBlank(message = "Airline shortName is required")
    @Size(max = 20, message = "Airline shortName must be <= 20 characters")
    private String shortName;

    @Size(max = 255, message = "Logo URL must be <= 255 characters")
    private String logo;
}
