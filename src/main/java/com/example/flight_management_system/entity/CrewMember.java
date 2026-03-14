package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.CrewRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    private CrewRole role;

    @Column(length = 40)
    private String licenseNumber;

    @Column(length = 40)
    private String nationality;

    /** Accumulated duty minutes in current 28-day cycle. */
    private int dutyMinutesThisCycle;

    /** Maximum allowable duty minutes per cycle (default 14h = 840 min). */
    @Builder.Default
    private int maxDutyMinutesPerCycle = 840;

    /** Minimum rest minutes between duties (default 11h = 660 min). */
    @Builder.Default
    private int minRestMinutesBetweenDuties = 660;

    /** Crew member must not be assigned duties before this timestamp. */
    private LocalDateTime restPeriodEnd;

    @Builder.Default
    private boolean available = true;

    @Column(length = 80)
    private String email;

    @Column(length = 20)
    private String phone;
}
