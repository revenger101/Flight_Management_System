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
public class CrewRoster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Enumerated(EnumType.STRING)
    private CrewRole roleOnFlight;

    @Builder.Default
    private boolean checkedIn = false;
    private LocalDateTime checkedInAt;

    /** Estimated flight + turnaround duty time for this assignment in minutes. */
    @Builder.Default
    private int estimatedDutyMinutes = 0;

    @Builder.Default
    private boolean dutyTimeCompliant = true;

    @Builder.Default
    private boolean restRuleCompliant = true;

    @Column(length = 255)
    private String notes;

    private LocalDateTime assignedAt;
}
