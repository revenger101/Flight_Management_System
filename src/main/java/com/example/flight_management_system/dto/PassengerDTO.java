package com.example.flight_management_system.dto;

import jakarta.validation.Valid;
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
public class PassengerDTO {
    private Long id;

    @NotBlank(message = "Passenger name is required")
    @Size(max = 120, message = "Passenger name must be <= 120 characters")
    private String name;

    @NotBlank(message = "CC is required")
    @Size(max = 30, message = "CC must be <= 30 characters")
    private String cc;

    @Size(max = 40, message = "mileCard must be <= 40 characters")
    private String mileCard;

    @Size(max = 30, message = "status must be <= 30 characters")
    private String status;

    @Valid
    private MilesAccountDTO milesAccount;
}
