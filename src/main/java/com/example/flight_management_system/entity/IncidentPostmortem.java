package com.example.flight_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPostmortem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ticket_id", unique = true)
    private IncidentTicket ticket;

    @Column(length = 2000)
    private String incidentSummary;

    @Column(length = 2000)
    private String rootCause;

    @Column(length = 2000)
    private String timeline;

    @Column(length = 120)
    private String createdBy;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "postmortem", cascade = CascadeType.ALL)
    private List<IncidentCorrectiveAction> correctiveActions;
}
