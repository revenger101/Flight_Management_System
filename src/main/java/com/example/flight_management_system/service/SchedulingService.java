package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.AircraftDTO;
import com.example.flight_management_system.dto.CrewMemberDTO;
import com.example.flight_management_system.dto.CrewRosterDTO;
import com.example.flight_management_system.dto.GateSlotDTO;
import com.example.flight_management_system.entity.*;
import com.example.flight_management_system.entity.enums.AircraftStatus;
import com.example.flight_management_system.entity.enums.GateSlotStatus;
import com.example.flight_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulingService {

    private final AircraftRepository aircraftRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRosterRepository crewRosterRepository;
    private final GateSlotRepository gateSlotRepository;
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    // ======================== AIRCRAFT ========================

    public List<AircraftDTO> getAircraft() {
        return aircraftRepository.findAll().stream()
                .map(a -> toAircraftDTO(a, findCurrentFlight(a)))
                .toList();
    }

    public AircraftDTO getAircraftById(Long id) {
        Aircraft aircraft = require(aircraftRepository.findById(id), "Aircraft", id);
        return toAircraftDTO(aircraft, findCurrentFlight(aircraft));
    }

    @Transactional
    public AircraftDTO createAircraft(AircraftDTO dto) {
        if (aircraftRepository.existsByRegistration(dto.getRegistration())) {
            throw new IllegalArgumentException("Registration already exists: " + dto.getRegistration());
        }
        Aircraft aircraft = Aircraft.builder()
                .registration(dto.getRegistration().toUpperCase())
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .totalSeats(dto.getTotalSeats())
                .economySeats(dto.getEconomySeats())
                .businessSeats(dto.getBusinessSeats())
                .status(dto.getStatus() != null ? dto.getStatus() : AircraftStatus.AVAILABLE)
                .nextMaintenanceAt(dto.getNextMaintenanceAt())
                .totalFlightHours(dto.getTotalFlightHours())
                .airlineCode(dto.getAirlineCode())
                .build();
        return toAircraftDTO(aircraftRepository.save(aircraft), null);
    }

    @Transactional
    public AircraftDTO updateAircraft(Long id, AircraftDTO dto) {
        Aircraft aircraft = require(aircraftRepository.findById(id), "Aircraft", id);
        aircraft.setModel(dto.getModel());
        aircraft.setManufacturer(dto.getManufacturer());
        aircraft.setTotalSeats(dto.getTotalSeats());
        aircraft.setEconomySeats(dto.getEconomySeats());
        aircraft.setBusinessSeats(dto.getBusinessSeats());
        aircraft.setStatus(dto.getStatus());
        aircraft.setNextMaintenanceAt(dto.getNextMaintenanceAt());
        aircraft.setTotalFlightHours(dto.getTotalFlightHours());
        aircraft.setAirlineCode(dto.getAirlineCode());
        return toAircraftDTO(aircraftRepository.save(aircraft), findCurrentFlight(aircraft));
    }

    @Transactional
    public void deleteAircraft(Long id) {
        Aircraft aircraft = require(aircraftRepository.findById(id), "Aircraft", id);
        for (Flight f : flightRepository.findByAircraft(aircraft)) {
            f.setAircraft(null);
            flightRepository.save(f);
        }
        aircraftRepository.delete(aircraft);
    }

    @Transactional
    public AircraftDTO assignAircraftToFlight(Long aircraftId, Long flightId) {
        Aircraft aircraft = require(aircraftRepository.findById(aircraftId), "Aircraft", aircraftId);
        Flight flight = require(flightRepository.findById(flightId), "Flight", flightId);
        flight.setAircraft(aircraft);
        aircraft.setStatus(AircraftStatus.IN_SERVICE);
        flightRepository.save(flight);
        return toAircraftDTO(aircraftRepository.save(aircraft), flight);
    }

    @Transactional
    public void unassignAircraftFromFlight(Long flightId) {
        Flight flight = require(flightRepository.findById(flightId), "Flight", flightId);
        if (flight.getAircraft() == null) return;
        Aircraft aircraft = flight.getAircraft();
        flight.setAircraft(null);
        flightRepository.save(flight);
        boolean stillInService = !flightRepository.findByAircraft(aircraft).isEmpty();
        if (!stillInService) {
            aircraft.setStatus(AircraftStatus.AVAILABLE);
            aircraftRepository.save(aircraft);
        }
    }

    // ======================== CREW MEMBERS ========================

    public List<CrewMemberDTO> getCrewMembers() {
        return crewMemberRepository.findAll().stream().map(this::toCrewMemberDTO).toList();
    }

    public CrewMemberDTO getCrewMemberById(Long id) {
        return toCrewMemberDTO(require(crewMemberRepository.findById(id), "CrewMember", id));
    }

    @Transactional
    public CrewMemberDTO createCrewMember(CrewMemberDTO dto) {
        CrewMember cm = CrewMember.builder()
                .name(dto.getName())
                .role(dto.getRole())
                .licenseNumber(dto.getLicenseNumber())
                .nationality(dto.getNationality())
                .dutyMinutesThisCycle(dto.getDutyMinutesThisCycle())
                .maxDutyMinutesPerCycle(dto.getMaxDutyMinutesPerCycle() > 0 ? dto.getMaxDutyMinutesPerCycle() : 840)
                .minRestMinutesBetweenDuties(dto.getMinRestMinutesBetweenDuties() > 0 ? dto.getMinRestMinutesBetweenDuties() : 660)
                .restPeriodEnd(dto.getRestPeriodEnd())
                .available(dto.isAvailable())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
        return toCrewMemberDTO(crewMemberRepository.save(cm));
    }

    @Transactional
    public CrewMemberDTO updateCrewMember(Long id, CrewMemberDTO dto) {
        CrewMember cm = require(crewMemberRepository.findById(id), "CrewMember", id);
        cm.setName(dto.getName());
        cm.setRole(dto.getRole());
        cm.setLicenseNumber(dto.getLicenseNumber());
        cm.setNationality(dto.getNationality());
        cm.setMaxDutyMinutesPerCycle(dto.getMaxDutyMinutesPerCycle() > 0 ? dto.getMaxDutyMinutesPerCycle() : 840);
        cm.setMinRestMinutesBetweenDuties(
                dto.getMinRestMinutesBetweenDuties() > 0 ? dto.getMinRestMinutesBetweenDuties() : 660);
        cm.setRestPeriodEnd(dto.getRestPeriodEnd());
        cm.setAvailable(dto.isAvailable());
        cm.setEmail(dto.getEmail());
        cm.setPhone(dto.getPhone());
        return toCrewMemberDTO(crewMemberRepository.save(cm));
    }

    @Transactional
    public void deleteCrewMember(Long id) {
        crewMemberRepository.deleteById(id);
    }

    // ======================== CREW ROSTER ========================

    public List<CrewRosterDTO> getRosterByFlight(Long flightId) {
        return crewRosterRepository.findByFlightId(flightId).stream()
                .map(this::toCrewRosterDTO).toList();
    }

    public List<CrewRosterDTO> getRosterByCrewMember(Long crewMemberId) {
        CrewMember cm = require(crewMemberRepository.findById(crewMemberId), "CrewMember", crewMemberId);
        return crewRosterRepository.findByCrewMember(cm).stream()
                .map(this::toCrewRosterDTO).toList();
    }

    @Transactional
    public CrewRosterDTO assignCrewToFlight(CrewRosterDTO dto) {
        CrewMember cm = require(crewMemberRepository.findById(dto.getCrewMemberId()), "CrewMember",
                dto.getCrewMemberId());
        Flight flight = require(flightRepository.findById(dto.getFlightId()), "Flight", dto.getFlightId());

        int newDutyTotal = cm.getDutyMinutesThisCycle() + dto.getEstimatedDutyMinutes();
        boolean dutyCompliant = newDutyTotal <= cm.getMaxDutyMinutesPerCycle();
        boolean restCompliant = cm.getRestPeriodEnd() == null
                || LocalDateTime.now().isAfter(cm.getRestPeriodEnd());

        CrewRoster roster = CrewRoster.builder()
                .crewMember(cm)
                .flight(flight)
                .roleOnFlight(dto.getRoleOnFlight() != null ? dto.getRoleOnFlight() : cm.getRole())
                .estimatedDutyMinutes(dto.getEstimatedDutyMinutes())
                .dutyTimeCompliant(dutyCompliant)
                .restRuleCompliant(restCompliant)
                .notes(dto.getNotes())
                .assignedAt(LocalDateTime.now())
                .build();

        return toCrewRosterDTO(crewRosterRepository.save(roster));
    }

    @Transactional
    public CrewRosterDTO checkInCrew(Long rosterId) {
        CrewRoster roster = require(crewRosterRepository.findById(rosterId), "CrewRoster", rosterId);
        roster.setCheckedIn(true);
        roster.setCheckedInAt(LocalDateTime.now());

        CrewMember cm = roster.getCrewMember();
        cm.setDutyMinutesThisCycle(cm.getDutyMinutesThisCycle() + roster.getEstimatedDutyMinutes());
        cm.setRestPeriodEnd(LocalDateTime.now().plusMinutes(cm.getMinRestMinutesBetweenDuties()));
        crewMemberRepository.save(cm);

        return toCrewRosterDTO(crewRosterRepository.save(roster));
    }

    @Transactional
    public void removeCrewFromFlight(Long rosterId) {
        crewRosterRepository.deleteById(rosterId);
    }

    // ======================== GATE SLOTS ========================

    public List<GateSlotDTO> getGateSlots() {
        return gateSlotRepository.findAll().stream().map(this::toGateSlotDTO).toList();
    }

    public List<GateSlotDTO> getGateSlotsByFlight(Long flightId) {
        return gateSlotRepository.findByFlightId(flightId).stream().map(this::toGateSlotDTO).toList();
    }

    public List<GateSlotDTO> getGateSlotsByAirport(Long airportId) {
        return gateSlotRepository.findByAirportId(airportId).stream().map(this::toGateSlotDTO).toList();
    }

    @Transactional
    public GateSlotDTO createGateSlot(GateSlotDTO dto) {
        Airport airport = require(airportRepository.findById(dto.getAirportId()), "Airport",
                dto.getAirportId());
        Flight flight = require(flightRepository.findById(dto.getFlightId()), "Flight", dto.getFlightId());

        boolean hasConflict = false;
        Long conflictingId = null;
        if (dto.getScheduledStart() != null && dto.getScheduledEnd() != null) {
            List<GateSlot> overlaps = gateSlotRepository
                    .findPotentialOverlaps(airport.getId(), dto.getGateNumber(),
                            dto.getScheduledStart(), dto.getScheduledEnd())
                    .stream()
                    .filter(s -> s.getStatus() != GateSlotStatus.CANCELLED)
                    .toList();
            hasConflict = !overlaps.isEmpty();
            if (hasConflict) {
                conflictingId = overlaps.get(0).getId();
                GateSlot existing = overlaps.get(0);
                existing.setConflict(true);
                gateSlotRepository.save(existing);
            }
        }

        GateSlot slot = GateSlot.builder()
                .airport(airport)
                .gateNumber(dto.getGateNumber())
                .flight(flight)
                .scheduledStart(dto.getScheduledStart())
                .scheduledEnd(dto.getScheduledEnd())
                .status(dto.getStatus() != null ? dto.getStatus() : GateSlotStatus.SCHEDULED)
                .conflict(hasConflict)
                .conflictingSlotId(conflictingId)
                .build();

        return toGateSlotDTO(gateSlotRepository.save(slot));
    }

    @Transactional
    public GateSlotDTO updateGateSlot(Long id, GateSlotDTO dto) {
        GateSlot slot = require(gateSlotRepository.findById(id), "GateSlot", id);
        slot.setActualStart(dto.getActualStart());
        slot.setActualEnd(dto.getActualEnd());
        if (dto.getStatus() != null) slot.setStatus(dto.getStatus());
        return toGateSlotDTO(gateSlotRepository.save(slot));
    }

    @Transactional
    public void deleteGateSlot(Long id) {
        gateSlotRepository.deleteById(id);
    }

    @Transactional
    public List<GateSlotDTO> detectConflicts(Long airportId) {
        List<GateSlot> slots = gateSlotRepository.findByAirportId(airportId).stream()
                .filter(s -> s.getStatus() != GateSlotStatus.CANCELLED)
                .sorted(Comparator.comparing(GateSlot::getScheduledStart,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<GateSlotDTO> conflicts = new ArrayList<>();
        for (GateSlot slot : slots) {
            if (slot.getScheduledStart() == null || slot.getScheduledEnd() == null) continue;
            List<GateSlot> overlaps = gateSlotRepository
                    .findPotentialOverlaps(airportId, slot.getGateNumber(),
                            slot.getScheduledStart(), slot.getScheduledEnd())
                    .stream()
                    .filter(s -> s.getStatus() != GateSlotStatus.CANCELLED)
                    .filter(s -> !s.getId().equals(slot.getId()))
                    .toList();
            boolean conflict = !overlaps.isEmpty();
            if (slot.isConflict() != conflict) {
                slot.setConflict(conflict);
                gateSlotRepository.save(slot);
            }
            if (conflict) conflicts.add(toGateSlotDTO(slot));
        }
        return conflicts;
    }

    // ======================== Helpers ========================

    private Flight findCurrentFlight(Aircraft aircraft) {
        List<Flight> flights = flightRepository.findByAircraft(aircraft);
        return flights.isEmpty() ? null : flights.get(0);
    }

    private <T> T require(java.util.Optional<T> opt, String entity, Long id) {
        return opt.orElseThrow(() -> new RuntimeException(entity + " not found: " + id));
    }

    private AircraftDTO toAircraftDTO(Aircraft a, Flight current) {
        return AircraftDTO.builder()
                .id(a.getId())
                .registration(a.getRegistration())
                .model(a.getModel())
                .manufacturer(a.getManufacturer())
                .totalSeats(a.getTotalSeats())
                .economySeats(a.getEconomySeats())
                .businessSeats(a.getBusinessSeats())
                .status(a.getStatus())
                .nextMaintenanceAt(a.getNextMaintenanceAt())
                .totalFlightHours(a.getTotalFlightHours())
                .airlineCode(a.getAirlineCode())
                .currentFlightId(current != null ? current.getId() : null)
                .currentFlightRoute(current != null ? routeLabel(current) : null)
                .build();
    }

    private CrewMemberDTO toCrewMemberDTO(CrewMember cm) {
        boolean dutyLimit = cm.getDutyMinutesThisCycle() >= cm.getMaxDutyMinutesPerCycle();
        boolean restActive = cm.getRestPeriodEnd() != null
                && LocalDateTime.now().isBefore(cm.getRestPeriodEnd());
        return CrewMemberDTO.builder()
                .id(cm.getId())
                .name(cm.getName())
                .role(cm.getRole())
                .licenseNumber(cm.getLicenseNumber())
                .nationality(cm.getNationality())
                .dutyMinutesThisCycle(cm.getDutyMinutesThisCycle())
                .maxDutyMinutesPerCycle(cm.getMaxDutyMinutesPerCycle())
                .minRestMinutesBetweenDuties(cm.getMinRestMinutesBetweenDuties())
                .restPeriodEnd(cm.getRestPeriodEnd())
                .available(cm.isAvailable())
                .email(cm.getEmail())
                .phone(cm.getPhone())
                .dutyLimitReached(dutyLimit)
                .restPeriodActive(restActive)
                .build();
    }

    private CrewRosterDTO toCrewRosterDTO(CrewRoster r) {
        return CrewRosterDTO.builder()
                .id(r.getId())
                .crewMemberId(r.getCrewMember() != null ? r.getCrewMember().getId() : null)
                .crewMemberName(r.getCrewMember() != null ? r.getCrewMember().getName() : null)
                .crewMemberRole(r.getCrewMember() != null ? r.getCrewMember().getRole() : null)
                .flightId(r.getFlight() != null ? r.getFlight().getId() : null)
                .flightRoute(r.getFlight() != null ? routeLabel(r.getFlight()) : null)
                .roleOnFlight(r.getRoleOnFlight())
                .checkedIn(r.isCheckedIn())
                .checkedInAt(r.getCheckedInAt())
                .estimatedDutyMinutes(r.getEstimatedDutyMinutes())
                .dutyTimeCompliant(r.isDutyTimeCompliant())
                .restRuleCompliant(r.isRestRuleCompliant())
                .notes(r.getNotes())
                .assignedAt(r.getAssignedAt())
                .build();
    }

    private GateSlotDTO toGateSlotDTO(GateSlot s) {
        return GateSlotDTO.builder()
                .id(s.getId())
                .airportId(s.getAirport() != null ? s.getAirport().getId() : null)
                .airportCode(s.getAirport() != null ? s.getAirport().getShortName() : null)
                .gateNumber(s.getGateNumber())
                .flightId(s.getFlight() != null ? s.getFlight().getId() : null)
                .flightRoute(s.getFlight() != null ? routeLabel(s.getFlight()) : null)
                .scheduledStart(s.getScheduledStart())
                .scheduledEnd(s.getScheduledEnd())
                .actualStart(s.getActualStart())
                .actualEnd(s.getActualEnd())
                .conflict(s.isConflict())
                .conflictingSlotId(s.getConflictingSlotId())
                .status(s.getStatus())
                .build();
    }

    private static String routeLabel(Flight f) {
        String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
        String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";
        return dep + " → " + arr;
    }
}
