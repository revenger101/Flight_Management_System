package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.FlightStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveFlightTrackDTO {
    private Long flightId;
    private String route;
    private String departureCode;
    private String arrivalCode;
    private FlightStatus status;

    private Double departureLat;
    private Double departureLon;
    private Double arrivalLat;
    private Double arrivalLon;

    private Double currentLat;
    private Double currentLon;
    private Double progress;

    private Integer altitudeFeet;
    private Integer groundSpeedKts;
}
