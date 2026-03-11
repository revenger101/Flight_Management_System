package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDTO {
    private Long id;
    private LocalTime time;
    private int miles;
    private Long departureAirportId;
    private Long arrivalAirportId;
    private List<FlightHandlingDTO> flightHandlings;
    private List<Long> connectingFlightIds;
}
