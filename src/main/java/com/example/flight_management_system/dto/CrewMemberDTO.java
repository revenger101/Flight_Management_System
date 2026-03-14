package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.CrewRole;
import jakarta.validation.constraints.NotBlank;
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
public class CrewMemberDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Role is required")
    private CrewRole role;

    private String licenseNumber;
    private String nationality;
    private int dutyMinutesThisCycle;
    private int maxDutyMinutesPerCycle;
    private int minRestMinutesBetweenDuties;
    private LocalDateTime restPeriodEnd;
    private boolean available;
    private String email;
    private String phone;

    // Computed compliance flags
    private boolean dutyLimitReached;
    private boolean restPeriodActive;
}
