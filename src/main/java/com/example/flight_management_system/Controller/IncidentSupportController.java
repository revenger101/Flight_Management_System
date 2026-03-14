package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.IncidentCorrectiveActionDTO;
import com.example.flight_management_system.dto.IncidentPostmortemDTO;
import com.example.flight_management_system.dto.IncidentTicketDTO;
import com.example.flight_management_system.dto.UpdateIncidentStatusDTO;
import com.example.flight_management_system.service.IncidentSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class IncidentSupportController {

    private final IncidentSupportService incidentSupportService;

    @GetMapping("/tickets")
    public ResponseEntity<List<IncidentTicketDTO>> listTickets() {
        return ResponseEntity.ok(incidentSupportService.listTickets());
    }

    @GetMapping("/tickets/breached")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IncidentTicketDTO>> breached() {
        return ResponseEntity.ok(incidentSupportService.breachedSlaTickets());
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<IncidentTicketDTO> getTicket(@PathVariable Long id) {
        return ResponseEntity.ok(incidentSupportService.getTicket(id));
    }

    @PostMapping("/tickets")
    public ResponseEntity<IncidentTicketDTO> createTicket(
            @RequestBody IncidentTicketDTO dto,
            @AuthenticationPrincipal UserDetails user
    ) {
        String actor = user != null ? user.getUsername() : "system";
        return ResponseEntity.ok(incidentSupportService.createTicket(dto, actor));
    }

    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<IncidentTicketDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateIncidentStatusDTO dto,
            @AuthenticationPrincipal UserDetails user
    ) {
        String actor = user != null ? user.getUsername() : "system";
        return ResponseEntity.ok(incidentSupportService.updateStatus(id, dto, actor));
    }

    @PutMapping("/tickets/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentTicketDTO> assign(
            @PathVariable Long id,
            @RequestParam String assignee
    ) {
        return ResponseEntity.ok(incidentSupportService.assign(id, assignee));
    }

    @GetMapping("/tickets/{ticketId}/postmortem")
    public ResponseEntity<IncidentPostmortemDTO> getPostmortem(@PathVariable Long ticketId) {
        return ResponseEntity.ok(incidentSupportService.getPostmortemByTicket(ticketId));
    }

    @PostMapping("/tickets/{ticketId}/postmortem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentPostmortemDTO> upsertPostmortem(
            @PathVariable Long ticketId,
            @RequestBody IncidentPostmortemDTO dto,
            @AuthenticationPrincipal UserDetails user
    ) {
        String actor = user != null ? user.getUsername() : "system";
        return ResponseEntity.ok(incidentSupportService.upsertPostmortem(ticketId, dto, actor));
    }

    @PostMapping("/tickets/{ticketId}/actions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentCorrectiveActionDTO> addAction(
            @PathVariable Long ticketId,
            @RequestBody IncidentCorrectiveActionDTO dto
    ) {
        return ResponseEntity.ok(incidentSupportService.addCorrectiveAction(ticketId, dto));
    }

    @PutMapping("/actions/{actionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IncidentCorrectiveActionDTO> updateAction(
            @PathVariable Long actionId,
            @RequestBody IncidentCorrectiveActionDTO dto
    ) {
        return ResponseEntity.ok(incidentSupportService.updateCorrectiveAction(actionId, dto));
    }
}
