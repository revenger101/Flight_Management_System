package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.BookingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "corporate_rate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String corporateCode;

    @Column(nullable = false, length = 120)
    private String companyName;

    @Column(nullable = false)
    private Double discountPercent;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;

    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingType bookingType;
}
