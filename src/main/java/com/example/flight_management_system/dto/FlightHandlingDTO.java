package com.example.flight_management_system.dto;

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
    private int boardingGate;
    private int delay;
    private LocalDate date;
    private LocalTime time;
}
