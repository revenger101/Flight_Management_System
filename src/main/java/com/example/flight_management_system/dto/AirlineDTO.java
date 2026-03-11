package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirlineDTO {
    private Long id;
    private String name;
    private String shortName;
    private String logo;
}
