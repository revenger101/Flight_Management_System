package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AircraftDTO;
import com.example.flight_management_system.dto.CrewMemberDTO;
import com.example.flight_management_system.dto.CrewRosterDTO;
import com.example.flight_management_system.dto.GateSlotDTO;
import com.example.flight_management_system.service.SchedulingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class SchedulingController {

    private final SchedulingService schedulingService;

    // ───── Aircraft ─────

    @GetMapping("/aircraft")
    public ResponseEntity<List<AircraftDTO>> getAircraft() {
        return ResponseEntity.ok(schedulingService.getAircraft());
    }

    @GetMapping("/aircraft/{id}")
    public ResponseEntity<AircraftDTO> getAircraftById(@PathVariable Long id) {
        return ResponseEntity.ok(schedulingService.getAircraftById(id));
    }

    @PostMapping("/aircraft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AircraftDTO> createAircraft(@Valid @RequestBody AircraftDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedulingService.createAircraft(dto));
    }

    @PutMapping("/aircraft/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AircraftDTO> updateAircraft(@PathVariable Long id,
                                                       @Valid @RequestBody AircraftDTO dto) {
        return ResponseEntity.ok(schedulingService.updateAircraft(id, dto));
    }

    @DeleteMapping("/aircraft/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAircraft(@PathVariable Long id) {
        schedulingService.deleteAircraft(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/aircraft/{aircraftId}/assign/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AircraftDTO> assignToFlight(@PathVariable Long aircraftId,
                                                       @PathVariable Long flightId) {
        return ResponseEntity.ok(schedulingService.assignAircraftToFlight(aircraftId, flightId));
    }

    @DeleteMapping("/aircraft/unassign/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignFromFlight(@PathVariable Long flightId) {
        schedulingService.unassignAircraftFromFlight(flightId);
        return ResponseEntity.noContent().build();
    }

    // ───── Crew Members ─────

    @GetMapping("/crew")
    public ResponseEntity<List<CrewMemberDTO>> getCrewMembers() {
        return ResponseEntity.ok(schedulingService.getCrewMembers());
    }

    @GetMapping("/crew/{id}")
    public ResponseEntity<CrewMemberDTO> getCrewMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(schedulingService.getCrewMemberById(id));
    }

    @PostMapping("/crew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrewMemberDTO> createCrewMember(@Valid @RequestBody CrewMemberDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedulingService.createCrewMember(dto));
    }

    @PutMapping("/crew/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrewMemberDTO> updateCrewMember(@PathVariable Long id,
                                                           @Valid @RequestBody CrewMemberDTO dto) {
        return ResponseEntity.ok(schedulingService.updateCrewMember(id, dto));
    }

    @DeleteMapping("/crew/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCrewMember(@PathVariable Long id) {
        schedulingService.deleteCrewMember(id);
        return ResponseEntity.noContent().build();
    }

    // ───── Crew Roster ─────

    @GetMapping("/roster/flight/{flightId}")
    public ResponseEntity<List<CrewRosterDTO>> getRosterByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(schedulingService.getRosterByFlight(flightId));
    }

    @GetMapping("/roster/crew/{crewMemberId}")
    public ResponseEntity<List<CrewRosterDTO>> getRosterByCrewMember(@PathVariable Long crewMemberId) {
        return ResponseEntity.ok(schedulingService.getRosterByCrewMember(crewMemberId));
    }

    @PostMapping("/roster")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrewRosterDTO> assignCrewToFlight(@Valid @RequestBody CrewRosterDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schedulingService.assignCrewToFlight(dto));
    }

    @PostMapping("/roster/{rosterId}/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CrewRosterDTO> checkInCrew(@PathVariable Long rosterId) {
        return ResponseEntity.ok(schedulingService.checkInCrew(rosterId));
    }

    @DeleteMapping("/roster/{rosterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeCrewFromFlight(@PathVariable Long rosterId) {
        schedulingService.removeCrewFromFlight(rosterId);
        return ResponseEntity.noContent().build();
    }

    // ───── Gate Slots ─────

    @GetMapping("/gates")
    public ResponseEntity<List<GateSlotDTO>> getGateSlots() {
        return ResponseEntity.ok(schedulingService.getGateSlots());
    }

    @GetMapping("/gates/flight/{flightId}")
    public ResponseEntity<List<GateSlotDTO>> getGateSlotsByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(schedulingService.getGateSlotsByFlight(flightId));
    }

    @GetMapping("/gates/airport/{airportId}")
    public ResponseEntity<List<GateSlotDTO>> getGateSlotsByAirport(@PathVariable Long airportId) {
        return ResponseEntity.ok(schedulingService.getGateSlotsByAirport(airportId));
    }

    @PostMapping("/gates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GateSlotDTO> createGateSlot(@Valid @RequestBody GateSlotDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schedulingService.createGateSlot(dto));
    }

    @PutMapping("/gates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GateSlotDTO> updateGateSlot(@PathVariable Long id,
                                                       @Valid @RequestBody GateSlotDTO dto) {
        return ResponseEntity.ok(schedulingService.updateGateSlot(id, dto));
    }

    @DeleteMapping("/gates/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGateSlot(@PathVariable Long id) {
        schedulingService.deleteGateSlot(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gates/detect-conflicts/{airportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GateSlotDTO>> detectConflicts(@PathVariable Long airportId) {
        return ResponseEntity.ok(schedulingService.detectConflicts(airportId));
    }
}
