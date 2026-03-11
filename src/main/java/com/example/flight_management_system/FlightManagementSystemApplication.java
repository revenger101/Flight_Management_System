package com.example.flight_management_system;

import com.example.flight_management_system.entity.*;
import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class FlightManagementSystemApplication {

    public static void main(String[] args) {

        SpringApplication.run(FlightManagementSystemApplication.class, args);
    }


}
