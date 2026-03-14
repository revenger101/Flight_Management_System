package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.GateSlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "airport_id", nullable = false)
    private Airport airport;

    private int gateNumber;

    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;

    @Builder.Default
    private boolean conflict = false;
    private Long conflictingSlotId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GateSlotStatus status = GateSlotStatus.SCHEDULED;
}
