package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisruptionPlanDTO {
    private Long disruptedFlightId;
    private String disruptedFlightRoute;
    private FlightStatus disruptedFlightStatus;
    private int disruptedFlightDelayMinutes;
    private int affectedPassengerCount;

    private List<AffectedBookingDTO> affectedBookings;
    private List<AlternativeFlightDTO> alternatives;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AffectedBookingDTO {
        private Long bookingId;
        private Long passengerId;
        private String passengerName;
        private String bookingType;
        private String currentStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlternativeFlightDTO {
        private Long flightId;
        private String route;
        private LocalDateTime scheduledDeparture;
        private int delayMinutes;
        private int availableSeats;
        private FlightStatus status;
        private String suitabilityNote;
    }
}
