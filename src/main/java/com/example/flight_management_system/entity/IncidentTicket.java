package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.IncidentPriority;
import com.example.flight_management_system.entity.enums.IncidentSeverity;
import com.example.flight_management_system.entity.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 180)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private Integer businessImpact;

    @Column(length = 120)
    private String openedBy;

    @Column(length = 120)
    private String assignedTo;

    private LocalDateTime openedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime slaDueAt;

    @Column(length = 1200)
    private String resolutionSummary;
}
