package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.AirlineDTO;
import com.example.flight_management_system.entity.Airline;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.AirlineRepository;
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
public class AirlineService {

    private final AirlineRepository airlineRepository;

    // Convertir Entity → DTO
    private AirlineDTO toDTO(Airline airline) {
        return AirlineDTO.builder()
                .id(airline.getId())
                .name(airline.getName())
                .shortName(airline.getShortName())
                .logo(airline.getLogo())
                .build();
    }

    // Convertir DTO → Entity
    private Airline toEntity(AirlineDTO dto) {
        return Airline.builder()
                .name(dto.getName())
                .shortName(dto.getShortName())
                .logo(dto.getLogo())
                .build();
    }

    public List<AirlineDTO> findAll() {
        return airlineRepository.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AirlineDTO findById(Long id) {
        Airline airline = airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found with id: " + id));
        return toDTO(airline);
    }

    public Page<AirlineDTO> search(String query, Pageable pageable) {
        String normalized = query == null ? "" : query.trim();
        return airlineRepository
                .findByNameContainingIgnoreCaseOrShortNameContainingIgnoreCase(normalized, normalized, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public AirlineDTO create(AirlineDTO dto) {
        Airline saved = airlineRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    @Transactional
    public AirlineDTO update(Long id, AirlineDTO dto) {
        Airline existing = airlineRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Airline not found with id: " + id));
        existing.setName(dto.getName());
        existing.setShortName(dto.getShortName());
        existing.setLogo(dto.getLogo());
        return toDTO(airlineRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        airlineRepository.deleteById(id);
    }
}
