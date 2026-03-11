package com.example.flight_management_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String cc;
    private String mileCard;
    private String status;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "miles_account_id", referencedColumnName = "id")
    private MilesAccount milesAccount;


    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Booking> bookings;
}
