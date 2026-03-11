package com.example.flight_management_system.entity;


import com.example.flight_management_system.entity.enums.BookingType;
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

    private String kind;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private BookingType type;


    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;


    @ManyToOne
    @JoinColumn(name = "flight_id")
    private Flight flight;
}
