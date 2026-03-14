package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fare_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingType bookingType;

    private Double baseFare;
    private Double baseFareMultiplier;

    @Column(nullable = false)
    private boolean refundable;

    @Column(nullable = false)
    private Double changeFee;

    @Column(nullable = false)
    private Integer includedBaggageKg;

    @Column(nullable = false)
    private Double extraBaggageFeePerKg;

    @Column(nullable = false, length = 3)
    private String currency;
}
