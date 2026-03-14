package com.example.flight_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shortName;
    private String name;
    private String country;
    private float fee;
    private Double latitude;
    private Double longitude;


    @OneToOne
    @JoinColumn(name = "airline_id")
    private Airline airline;


    @OneToMany(mappedBy = "departureAirport")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Flight> departingFlights;


    @OneToMany(mappedBy = "arrivalAirport")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Flight> arrivingFlights;
}
