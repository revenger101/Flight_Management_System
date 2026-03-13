package com.example.flight_management_system.entity;


import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String kind;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private BookingType type;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Column(length = 80, unique = true)
    private String idempotencyKey;

    private String cancellationReason;
    private Long rebookedToFlightId;


    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;


    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @OneToMany(mappedBy = "booking")
    private java.util.List<LoyaltyLedger> loyaltyLedgers;

    @OneToMany(mappedBy = "booking")
    private java.util.List<NotificationEvent> notificationEvents;
}
