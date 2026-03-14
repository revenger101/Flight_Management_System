package com.example.flight_management_system.entity;


import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(length = 8)
    private String seatNumber;

    private boolean checkedIn;
    private LocalDateTime checkedInAt;

    @Column(length = 80)
    private String boardingPassCode;

    private String cancellationReason;
    private Long rebookedToFlightId;

    private Double baseFare;
    private Double finalFare;

    @Column(length = 3)
    private String currency;

    private boolean refundable;
    private Double changeFee;
    private Integer includedBaggageKg;
    private Integer baggageKg;
    private Double extraBaggageFee;

    @Column(length = 40)
    private String promoCode;

    @Column(length = 40)
    private String corporateCode;

    @Column(length = 80)
    private String campaignName;


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

    @OneToMany(mappedBy = "booking")
    private java.util.List<PassengerServiceRequest> serviceRequests;

    @OneToMany(mappedBy = "booking")
    private java.util.List<PaymentTransaction> paymentTransactions;
}
