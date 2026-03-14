package com.example.flight_management_system.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardingPassDTO {
    private Long bookingId;
    private String passengerName;
    private String route;
    private String seatNumber;
    private String gate;
    private LocalDateTime departure;
    private String boardingPassCode;
}
