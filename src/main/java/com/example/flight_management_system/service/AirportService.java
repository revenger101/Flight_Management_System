package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.AirportDTO;
import com.example.flight_management_system.entity.Airline;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.repository.AirlineRepository;
import com.example.flight_management_system.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;
    private final AirlineRepository airlineRepository;

    private AirportDTO toDTO(Airport airport) {
        return AirportDTO.builder()
                .id(airport.getId())
                .name(airport.getName())
                .shortName(airport.getShortName())
                .country(airport.getCountry())
                .fee(airport.getFee())
                .airlineId(airport.getAirline() != null ? airport.getAirline().getId() : null)
                .build();
    }

    private Airport toEntity(AirportDTO dto) {
        Airline airline = null;
        if (dto.getAirlineId() != null) {
            airline = airlineRepository.findById(dto.getAirlineId())
                    .orElseThrow(() -> new RuntimeException("Airline not found with id: " + dto.getAirlineId()));
        }
        return Airport.builder()
                .name(dto.getName())
                .shortName(dto.getShortName())
                .country(dto.getCountry())
                .fee(dto.getFee())
                .airline(airline)
                .build();
    }

    public List<AirportDTO> findAll() {
        return airportRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AirportDTO findById(Long id) {
        return toDTO(airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id)));
    }

    public AirportDTO create(AirportDTO dto) {
        return toDTO(airportRepository.save(toEntity(dto)));
    }

    public AirportDTO update(Long id, AirportDTO dto) {
        Airport existing = airportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Airport not found with id: " + id));
        existing.setName(dto.getName());
        existing.setShortName(dto.getShortName());
        existing.setCountry(dto.getCountry());
        existing.setFee(dto.getFee());
        if (dto.getAirlineId() != null) {
            Airline airline = airlineRepository.findById(dto.getAirlineId())
                    .orElseThrow(() -> new RuntimeException("Airline not found"));
            existing.setAirline(airline);
        }
        return toDTO(airportRepository.save(existing));
    }

    public void delete(Long id) {
        airportRepository.deleteById(id);
    }
}
