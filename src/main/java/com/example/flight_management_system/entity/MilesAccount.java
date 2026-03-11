package com.example.flight_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilesAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;
    private int flightMiles;
    private int statusMiles;


    @OneToOne(mappedBy = "milesAccount")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Passenger passenger;
}
