package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.FlightDTO;
import com.example.flight_management_system.dto.FlightHandlingDTO;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.FlightHandling;
import com.example.flight_management_system.repository.AirportRepository;
import com.example.flight_management_system.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    private FlightHandlingDTO toHandlingDTO(FlightHandling fh) {
        return FlightHandlingDTO.builder()
                .id(fh.getId())
                .boardingGate(fh.getBoardingGate())
                .delay(fh.getDelay())
                .date(fh.getDate())
                .time(fh.getTime())
                .build();
    }

    private FlightDTO toDTO(Flight flight) {
        return FlightDTO.builder()
                .id(flight.getId())
                .time(flight.getTime())
                .miles(flight.getMiles())
                .departureAirportId(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getId() : null)
                .arrivalAirportId(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getId() : null)
                .flightHandlings(flight.getFlightHandlings() != null ?
                        flight.getFlightHandlings().stream().map(this::toHandlingDTO).collect(Collectors.toList())
                        : new ArrayList<>())
                .connectingFlightIds(flight.getConnectingFlights() != null ?
                        flight.getConnectingFlights().stream().map(Flight::getId).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public List<FlightDTO> findAll() {
        return flightRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FlightDTO findById(Long id) {
        return toDTO(flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id)));
    }

    public FlightDTO create(FlightDTO dto) {
        Airport departure = airportRepository.findById(dto.getDepartureAirportId())
                .orElseThrow(() -> new RuntimeException("Departure airport not found"));
        Airport arrival = airportRepository.findById(dto.getArrivalAirportId())
                .orElseThrow(() -> new RuntimeException("Arrival airport not found"));

        List<FlightHandling> handlings = new ArrayList<>();
        if (dto.getFlightHandlings() != null) {
            handlings = dto.getFlightHandlings().stream().map(fhDTO ->
                    FlightHandling.builder()
                            .boardingGate(fhDTO.getBoardingGate())
                            .delay(fhDTO.getDelay())
                            .date(fhDTO.getDate())
                            .time(fhDTO.getTime())
                            .build()
            ).collect(Collectors.toList());
        }

        Flight flight = Flight.builder()
                .time(dto.getTime())
                .miles(dto.getMiles())
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .flightHandlings(handlings)
                .build();

        return toDTO(flightRepository.save(flight));
    }

    public FlightDTO update(Long id, FlightDTO dto) {
        Flight existing = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));
        existing.setTime(dto.getTime());
        existing.setMiles(dto.getMiles());
        if (dto.getDepartureAirportId() != null) {
            existing.setDepartureAirport(airportRepository.findById(dto.getDepartureAirportId())
                    .orElseThrow(() -> new RuntimeException("Departure airport not found")));
        }
        if (dto.getArrivalAirportId() != null) {
            existing.setArrivalAirport(airportRepository.findById(dto.getArrivalAirportId())
                    .orElseThrow(() -> new RuntimeException("Arrival airport not found")));
        }
        return toDTO(flightRepository.save(existing));
    }

    // Ajouter un vol connecté (ManyToMany)
    public FlightDTO addConnectingFlight(Long flightId, Long connectedFlightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        Flight connected = flightRepository.findById(connectedFlightId)
                .orElseThrow(() -> new RuntimeException("Connected flight not found"));

        if (flight.getConnectingFlights() == null) flight.setConnectingFlights(new ArrayList<>());
        flight.getConnectingFlights().add(connected);
        return toDTO(flightRepository.save(flight));
    }

    public void delete(Long id) {
        flightRepository.deleteById(id);
    }
}
