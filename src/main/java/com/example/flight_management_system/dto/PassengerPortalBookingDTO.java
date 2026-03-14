package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.BookingType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerPortalBookingDTO {
    private Long bookingId;
    private Long passengerId;
    private String passengerName;
    private Long flightId;
    private String route;
    private LocalDate date;
    private BookingType type;
    private BookingStatus status;
    private String seatNumber;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String boardingPassCode;
    private Double finalFare;
    private String currency;
}
