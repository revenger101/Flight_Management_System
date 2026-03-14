package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.CorrectiveActionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentCorrectiveAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "postmortem_id")
    private IncidentPostmortem postmortem;

    @Column(length = 1500)
    private String actionItem;

    @Column(length = 120)
    private String owner;

    private LocalDateTime dueAt;

    @Enumerated(EnumType.STRING)
    private CorrectiveActionStatus status;

    private LocalDateTime completedAt;
}
