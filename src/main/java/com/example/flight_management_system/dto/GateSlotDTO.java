package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.GateSlotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateSlotDTO {
    private Long id;

    @NotNull
    private Long airportId;
    private String airportCode;

    @NotNull
    private Integer gateNumber;

    @NotNull
    private Long flightId;
    private String flightRoute;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private boolean conflict;
    private Long conflictingSlotId;
    private GateSlotStatus status;
}
