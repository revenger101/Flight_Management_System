package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.BookingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private String kind;
    private LocalDate date;
    private BookingType type;
    private Long passengerId;
    private Long flightId;
}
