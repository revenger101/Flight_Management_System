package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.AircraftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String registration;

    @Column(length = 60)
    private String model;

    @Column(length = 40)
    private String manufacturer;

    private int totalSeats;
    private int economySeats;
    private int businessSeats;

    @Enumerated(EnumType.STRING)
    private AircraftStatus status;

    private LocalDateTime nextMaintenanceAt;
    private int totalFlightHours;

    @Column(length = 10)
    private String airlineCode;
}
