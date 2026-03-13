package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String message;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;
}
