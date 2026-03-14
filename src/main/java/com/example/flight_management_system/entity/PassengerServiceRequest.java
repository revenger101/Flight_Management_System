package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.ServiceRequestStatus;
import com.example.flight_management_system.entity.enums.ServiceRequestType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ServiceRequestType type;

    @Enumerated(EnumType.STRING)
    private ServiceRequestStatus status;

    @Column(length = 1000)
    private String reason;

    @Column(length = 1000)
    private String resolution;

    private Double requestedAmount;
    private Double approvedAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
}
