package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.LoyaltyLedgerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LoyaltyLedgerType type;

    private int miles;
    private String note;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
