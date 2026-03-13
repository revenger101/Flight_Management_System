package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.MilesAccountDTO;
import com.example.flight_management_system.dto.PassengerDTO;
import com.example.flight_management_system.entity.MilesAccount;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.PassengerRepository;
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
public class PassengerService {

    private final PassengerRepository passengerRepository;

    private MilesAccountDTO toMilesDTO(MilesAccount m) {
        if (m == null) return null;
        return MilesAccountDTO.builder()
                .id(m.getId())
                .number(m.getNumber())
                .flightMiles(m.getFlightMiles())
                .statusMiles(m.getStatusMiles())
                .build();
    }

    private PassengerDTO toDTO(Passenger p) {
        return PassengerDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .cc(p.getCc())
                .mileCard(p.getMileCard())
                .status(p.getStatus())
                .milesAccount(toMilesDTO(p.getMilesAccount()))
                .build();
    }

    public List<PassengerDTO> findAll() {
        return passengerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PassengerDTO findById(Long id) {
        return toDTO(passengerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + id)));
    }

    public Page<PassengerDTO> search(String name, String status, Pageable pageable) {
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim();
        return passengerRepository.search(normalizedName, normalizedStatus, pageable).map(this::toDTO);
    }

    @Transactional
    public PassengerDTO create(PassengerDTO dto) {
        MilesAccount miles = null;
        if (dto.getMilesAccount() != null) {
            miles = MilesAccount.builder()
                    .number(dto.getMilesAccount().getNumber())
                    .flightMiles(dto.getMilesAccount().getFlightMiles())
                    .statusMiles(dto.getMilesAccount().getStatusMiles())
                    .build();
        }
        Passenger passenger = Passenger.builder()
                .name(dto.getName())
                .cc(dto.getCc())
                .mileCard(dto.getMileCard())
                .status(dto.getStatus())
                .milesAccount(miles) // cascade sauvegarde automatiquement
                .build();
        return toDTO(passengerRepository.save(passenger));
    }

    @Transactional
    public PassengerDTO update(Long id, PassengerDTO dto) {
        Passenger existing = passengerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + id));
        existing.setName(dto.getName());
        existing.setCc(dto.getCc());
        existing.setMileCard(dto.getMileCard());
        existing.setStatus(dto.getStatus());
        return toDTO(passengerRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        passengerRepository.deleteById(id);
    }
}
