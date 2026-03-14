package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveFlightBoardDTO {
    private Long flightId;
    private String route;
    private String departureCode;
    private String arrivalCode;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedDeparture;
    private int delayMinutes;
    private FlightStatus status;
    private Integer gateNumber;
    private String aircraftRegistration;
    private String aircraftModel;
    private int totalSeats;
    private int confirmedBookings;
    private int seatsAvailable;

    // Crew readiness
    private int crewAssigned;
    private int crewCheckedIn;
    private boolean crewReady;

    // Turnaround
    private int turnaroundMinutes;
    private int turnaroundElapsedMinutes;
    private int turnaroundPercentComplete;

    // Alerts
    private int activeAlertCount;
    private String highestAlertSeverity;

    // Connecting impact
    private int affectedConnectingFlights;
}
