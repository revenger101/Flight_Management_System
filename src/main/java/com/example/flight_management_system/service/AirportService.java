package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.AirportDTO;
import com.example.flight_management_system.entity.Airline;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.AirlineRepository;
import com.example.flight_management_system.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
                .latitude(airport.getLatitude())
                .longitude(airport.getLongitude())
                .airlineId(airport.getAirline() != null ? airport.getAirline().getId() : null)
                .build();
    }

    private Airport toEntity(AirportDTO dto) {
        Airline airline = null;
        if (dto.getAirlineId() != null) {
            airline = airlineRepository.findById(dto.getAirlineId())
                    .orElseThrow(() -> new NotFoundException("Airline not found with id: " + dto.getAirlineId()));
        }
        return Airport.builder()
                .name(dto.getName())
                .shortName(dto.getShortName())
                .country(dto.getCountry())
                .fee(dto.getFee())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
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
                .orElseThrow(() -> new NotFoundException("Airport not found with id: " + id)));
    }

    public Page<AirportDTO> search(String country, String name, Long airlineId, Pageable pageable) {
        String normalizedCountry = country == null || country.isBlank() ? null : country.trim();
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        return airportRepository.search(normalizedCountry, normalizedName, airlineId, pageable).map(this::toDTO);
    }

    @Transactional
    public AirportDTO create(AirportDTO dto) {
        return toDTO(airportRepository.save(toEntity(dto)));
    }

    @Transactional
    public AirportDTO update(Long id, AirportDTO dto) {
        Airport existing = airportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airport not found with id: " + id));
        existing.setName(dto.getName());
        existing.setShortName(dto.getShortName());
        existing.setCountry(dto.getCountry());
        existing.setFee(dto.getFee());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());
        if (dto.getAirlineId() != null) {
            Airline airline = airlineRepository.findById(dto.getAirlineId())
                    .orElseThrow(() -> new NotFoundException("Airline not found"));
            existing.setAirline(airline);
        }
        return toDTO(airportRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        airportRepository.deleteById(id);
    }
}
