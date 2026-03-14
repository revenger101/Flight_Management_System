package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationalAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Column(length = 255)
    private String message;

    @Column(length = 500)
    private String details;

    private LocalDateTime triggeredAt;
    private LocalDateTime slaDeadline;
    private LocalDateTime resolvedAt;

    @Builder.Default
    private boolean resolved = false;

    @Column(length = 80)
    private String resolvedBy;

    @Column(length = 255)
    private String resolution;
}
