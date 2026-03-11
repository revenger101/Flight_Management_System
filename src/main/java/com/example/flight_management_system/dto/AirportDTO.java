package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportDTO {
    private Long id;
    private String shortName;
    private String name;
    private String country;
    private float fee;
    private Long airlineId;
}
