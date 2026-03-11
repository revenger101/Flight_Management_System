package com.example.flight_management_system.dto;

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
    private String number;
    private int flightMiles;
    private int statusMiles;
}
