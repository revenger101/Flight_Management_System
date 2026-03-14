package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.IncidentCorrectiveActionDTO;
import com.example.flight_management_system.dto.IncidentPostmortemDTO;
import com.example.flight_management_system.dto.IncidentTicketDTO;
import com.example.flight_management_system.dto.UpdateIncidentStatusDTO;
import com.example.flight_management_system.entity.IncidentCorrectiveAction;
import com.example.flight_management_system.entity.IncidentPostmortem;
import com.example.flight_management_system.entity.IncidentTicket;
import com.example.flight_management_system.entity.enums.CorrectiveActionStatus;
import com.example.flight_management_system.entity.enums.IncidentPriority;
import com.example.flight_management_system.entity.enums.IncidentSeverity;
import com.example.flight_management_system.entity.enums.IncidentStatus;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.IncidentCorrectiveActionRepository;
import com.example.flight_management_system.repository.IncidentPostmortemRepository;
import com.example.flight_management_system.repository.IncidentTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentSupportService {

    private final IncidentTicketRepository ticketRepository;
    private final IncidentPostmortemRepository postmortemRepository;
    private final IncidentCorrectiveActionRepository correctiveActionRepository;

    public List<IncidentTicketDTO> listTickets() {
        return ticketRepository.findAll().stream().map(this::toTicketDTO).toList();
    }

    public IncidentTicketDTO getTicket(Long id) {
        return toTicketDTO(getTicketEntity(id));
    }

    public List<IncidentTicketDTO> breachedSlaTickets() {
        List<IncidentStatus> active = List.of(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS);
        return ticketRepository.findBySlaDueAtBeforeAndStatusIn(LocalDateTime.now(), active).stream()
                .map(this::toTicketDTO)
                .toList();
    }

    @Transactional
    public IncidentTicketDTO createTicket(IncidentTicketDTO dto, String actor) {
        if (dto.getTitle() == null || dto.getTitle().isBlank() || dto.getSeverity() == null) {
            throw new BadRequestException("title and severity are required");
        }

        int impact = dto.getBusinessImpact() != null ? dto.getBusinessImpact() : 1;
        IncidentPriority priority = calculatePriority(dto.getSeverity(), impact);

        IncidentTicket ticket = IncidentTicket.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .businessImpact(impact)
                .priority(priority)
                .status(IncidentStatus.OPEN)
                .openedBy(actor)
                .assignedTo(dto.getAssignedTo())
                .openedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .slaDueAt(LocalDateTime.now().plusHours(slaHours(priority)))
                .build();

        return toTicketDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public IncidentTicketDTO updateStatus(Long ticketId, UpdateIncidentStatusDTO dto, String actor) {
        IncidentTicket ticket = getTicketEntity(ticketId);
        if (dto == null || dto.getStatus() == null) {
            throw new BadRequestException("status is required");
        }

        ticket.setStatus(dto.getStatus());
        ticket.setUpdatedAt(LocalDateTime.now());
        if (dto.getResolutionSummary() != null) {
            ticket.setResolutionSummary(dto.getResolutionSummary());
        }
        if (dto.getStatus() == IncidentStatus.RESOLVED || dto.getStatus() == IncidentStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
            if (ticket.getResolutionSummary() == null || ticket.getResolutionSummary().isBlank()) {
                ticket.setResolutionSummary("Resolved by " + actor);
            }
        }
        return toTicketDTO(ticketRepository.save(ticket));
    }

    @Transactional
    public IncidentTicketDTO assign(Long ticketId, String assignee) {
        IncidentTicket ticket = getTicketEntity(ticketId);
        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());
        if (ticket.getStatus() == IncidentStatus.OPEN) {
            ticket.setStatus(IncidentStatus.IN_PROGRESS);
        }
        return toTicketDTO(ticketRepository.save(ticket));
    }

    public IncidentPostmortemDTO getPostmortemByTicket(Long ticketId) {
        return postmortemRepository.findByTicketId(ticketId)
                .map(this::toPostmortemDTO)
                .orElse(null);
    }

    @Transactional
    public IncidentPostmortemDTO upsertPostmortem(Long ticketId, IncidentPostmortemDTO dto, String actor) {
        IncidentTicket ticket = getTicketEntity(ticketId);

        IncidentPostmortem postmortem = postmortemRepository.findByTicketId(ticketId)
                .orElse(IncidentPostmortem.builder()
                        .ticket(ticket)
                        .createdBy(actor)
                        .createdAt(LocalDateTime.now())
                        .build());

        postmortem.setIncidentSummary(dto.getIncidentSummary());
        postmortem.setRootCause(dto.getRootCause());
        postmortem.setTimeline(dto.getTimeline());
        IncidentPostmortem saved = postmortemRepository.save(postmortem);
        return toPostmortemDTO(saved);
    }

    @Transactional
    public IncidentCorrectiveActionDTO addCorrectiveAction(Long ticketId, IncidentCorrectiveActionDTO dto) {
        IncidentPostmortem postmortem = postmortemRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new NotFoundException("Postmortem not found for ticket: " + ticketId));

        IncidentCorrectiveAction action = IncidentCorrectiveAction.builder()
                .postmortem(postmortem)
                .actionItem(dto.getActionItem())
                .owner(dto.getOwner())
                .dueAt(dto.getDueAt())
                .status(dto.getStatus() != null ? dto.getStatus() : CorrectiveActionStatus.OPEN)
                .completedAt(dto.getCompletedAt())
                .build();

        return toActionDTO(correctiveActionRepository.save(action));
    }

    @Transactional
    public IncidentCorrectiveActionDTO updateCorrectiveAction(Long actionId, IncidentCorrectiveActionDTO dto) {
        IncidentCorrectiveAction action = correctiveActionRepository.findById(actionId)
                .orElseThrow(() -> new NotFoundException("Corrective action not found with id: " + actionId));

        if (dto.getActionItem() != null) action.setActionItem(dto.getActionItem());
        if (dto.getOwner() != null) action.setOwner(dto.getOwner());
        if (dto.getDueAt() != null) action.setDueAt(dto.getDueAt());
        if (dto.getStatus() != null) {
            action.setStatus(dto.getStatus());
            if (dto.getStatus() == CorrectiveActionStatus.DONE) {
                action.setCompletedAt(LocalDateTime.now());
            }
        }

        return toActionDTO(correctiveActionRepository.save(action));
    }

    private IncidentTicket getTicketEntity(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Incident ticket not found with id: " + id));
    }

    private IncidentPriority calculatePriority(IncidentSeverity severity, int impact) {
        int score = switch (severity) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
        int total = score + Math.max(1, Math.min(5, impact));

        if (total >= 8) return IncidentPriority.P1_CRITICAL;
        if (total >= 6) return IncidentPriority.P2_HIGH;
        if (total >= 4) return IncidentPriority.P3_MODERATE;
        return IncidentPriority.P4_LOW;
    }

    private int slaHours(IncidentPriority priority) {
        return switch (priority) {
            case P1_CRITICAL -> 2;
            case P2_HIGH -> 6;
            case P3_MODERATE -> 24;
            case P4_LOW -> 72;
        };
    }

    private IncidentTicketDTO toTicketDTO(IncidentTicket ticket) {
        boolean breached = ticket.getSlaDueAt() != null
                && ticket.getSlaDueAt().isBefore(LocalDateTime.now())
                && (ticket.getStatus() == IncidentStatus.OPEN || ticket.getStatus() == IncidentStatus.IN_PROGRESS);

        return IncidentTicketDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .severity(ticket.getSeverity())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .businessImpact(ticket.getBusinessImpact())
                .openedBy(ticket.getOpenedBy())
                .assignedTo(ticket.getAssignedTo())
                .openedAt(ticket.getOpenedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .slaDueAt(ticket.getSlaDueAt())
                .slaBreached(breached)
                .resolutionSummary(ticket.getResolutionSummary())
                .build();
    }

    private IncidentPostmortemDTO toPostmortemDTO(IncidentPostmortem postmortem) {
        List<IncidentCorrectiveActionDTO> actions = correctiveActionRepository
                .findByPostmortemIdOrderByDueAtAsc(postmortem.getId())
                .stream()
                .map(this::toActionDTO)
                .toList();

        return IncidentPostmortemDTO.builder()
                .id(postmortem.getId())
                .ticketId(postmortem.getTicket() != null ? postmortem.getTicket().getId() : null)
                .incidentSummary(postmortem.getIncidentSummary())
                .rootCause(postmortem.getRootCause())
                .timeline(postmortem.getTimeline())
                .createdBy(postmortem.getCreatedBy())
                .createdAt(postmortem.getCreatedAt())
                .correctiveActions(actions)
                .build();
    }

    private IncidentCorrectiveActionDTO toActionDTO(IncidentCorrectiveAction action) {
        return IncidentCorrectiveActionDTO.builder()
                .id(action.getId())
                .postmortemId(action.getPostmortem() != null ? action.getPostmortem().getId() : null)
                .actionItem(action.getActionItem())
                .owner(action.getOwner())
                .dueAt(action.getDueAt())
                .status(action.getStatus())
                .completedAt(action.getCompletedAt())
                .build();
    }
}
