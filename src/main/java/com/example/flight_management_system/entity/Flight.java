package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.FlightStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private LocalTime time;
    private int miles;
    private int seatCapacity;
    private int overbookingLimit;
    private boolean waitlistEnabled;
    private Integer currentGate;
    private int delayMinutes;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;


    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "flight_id")
    private List<FlightHandling> flightHandlings;


    @ManyToMany
    @JoinTable(
            name = "connecting_flights",
            joinColumns = @JoinColumn(name = "flight_id"),
            inverseJoinColumns = @JoinColumn(name = "connected_flight_id")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Flight> connectingFlights;

    @ManyToMany(mappedBy = "connectingFlights")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Flight> connectedByFlights;


    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;


    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;


    @OneToMany(mappedBy = "flight")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Booking> bookings;

    @OneToMany(mappedBy = "flight")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<NotificationEvent> notificationEvents;
}
