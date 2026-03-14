package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.CrewRole;
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
public class CrewRosterDTO {
    private Long id;

    @NotNull
    private Long crewMemberId;
    private String crewMemberName;
    private CrewRole crewMemberRole;

    @NotNull
    private Long flightId;
    private String flightRoute;

    private CrewRole roleOnFlight;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;
    private int estimatedDutyMinutes;
    private boolean dutyTimeCompliant;
    private boolean restRuleCompliant;
    private String notes;
    private LocalDateTime assignedAt;
}
