package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.ApprovalWorkflowDTO;
import com.example.flight_management_system.dto.WorkflowAuditTrailDTO;
import com.example.flight_management_system.dto.WorkflowTransitionRequestDTO;
import com.example.flight_management_system.entity.ApprovalWorkflow;
import com.example.flight_management_system.entity.WorkflowAuditTrail;
import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.entity.enums.WorkflowState;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.ApprovalWorkflowRepository;
import com.example.flight_management_system.repository.WorkflowAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkflowService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final WorkflowAuditTrailRepository auditTrailRepository;

    public List<ApprovalWorkflowDTO> listWorkflows() {
        return workflowRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ApprovalWorkflowDTO getWorkflow(Long id) {
        return toDTO(getEntity(id));
    }

    public List<WorkflowAuditTrailDTO> auditTrail(Long workflowId) {
        return auditTrailRepository.findByWorkflowIdOrderByChangedAtDesc(workflowId)
                .stream().map(this::toAuditDTO).toList();
    }

    @Transactional
    public ApprovalWorkflowDTO createDraft(ApprovalWorkflowDTO dto, UserDetails user) {
        if (dto.getWorkflowType() == null) {
            throw new BadRequestException("workflowType is required");
        }

        ApprovalWorkflow entity = ApprovalWorkflow.builder()
                .workflowType(dto.getWorkflowType())
                .state(WorkflowState.DRAFT)
                .requiredApprovalRole(dto.getRequiredApprovalRole() != null ? dto.getRequiredApprovalRole() : UserRole.ADMIN)
                .targetEntity(dto.getTargetEntity())
                .targetEntityId(dto.getTargetEntityId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .oldValues(dto.getOldValues())
                .newValues(dto.getNewValues())
                .createdBy(actor(user))
                .createdAt(LocalDateTime.now())
                .build();

        ApprovalWorkflow saved = workflowRepository.save(entity);
        audit(saved, "CREATE_DRAFT", null, WorkflowState.DRAFT, saved.getOldValues(), saved.getNewValues(), actor(user), "Draft created");
        return toDTO(saved);
    }

    @Transactional
    public ApprovalWorkflowDTO submitForReview(Long workflowId, UserDetails user, WorkflowTransitionRequestDTO request) {
        ApprovalWorkflow workflow = getEntity(workflowId);
        ensureState(workflow, WorkflowState.DRAFT, "Only draft workflow can be submitted for review");

        WorkflowState old = workflow.getState();
        workflow.setState(WorkflowState.REVIEW);
        workflow.setReviewComment(request != null ? request.getNote() : null);
        workflow.setReviewedBy(actor(user));
        workflow.setReviewedAt(LocalDateTime.now());

        ApprovalWorkflow saved = workflowRepository.save(workflow);
        audit(saved, "SUBMIT_REVIEW", old, saved.getState(), saved.getOldValues(), saved.getNewValues(), actor(user), saved.getReviewComment());
        return toDTO(saved);
    }

    @Transactional
    public ApprovalWorkflowDTO approve(Long workflowId, UserDetails user, WorkflowTransitionRequestDTO request) {
        ApprovalWorkflow workflow = getEntity(workflowId);
        ensureState(workflow, WorkflowState.REVIEW, "Only review workflow can be approved");
        ensureRole(user, workflow.getRequiredApprovalRole());

        WorkflowState old = workflow.getState();
        workflow.setState(WorkflowState.APPROVED);
        workflow.setApprovedBy(actor(user));
        workflow.setApprovedAt(LocalDateTime.now());
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            workflow.setReviewComment(request.getNote());
        }

        ApprovalWorkflow saved = workflowRepository.save(workflow);
        audit(saved, "APPROVE", old, saved.getState(), saved.getOldValues(), saved.getNewValues(), actor(user), workflow.getReviewComment());
        return toDTO(saved);
    }

    @Transactional
    public ApprovalWorkflowDTO execute(Long workflowId, UserDetails user, WorkflowTransitionRequestDTO request) {
        ApprovalWorkflow workflow = getEntity(workflowId);
        ensureState(workflow, WorkflowState.APPROVED, "Only approved workflow can be executed");
        ensureRole(user, workflow.getRequiredApprovalRole());

        WorkflowState old = workflow.getState();
        workflow.setState(WorkflowState.EXECUTED);
        workflow.setExecutedBy(actor(user));
        workflow.setExecutedAt(LocalDateTime.now());
        workflow.setExecutionNote(request != null ? request.getNote() : null);

        ApprovalWorkflow saved = workflowRepository.save(workflow);
        audit(saved, "EXECUTE", old, saved.getState(), saved.getOldValues(), saved.getNewValues(), actor(user), workflow.getExecutionNote());
        return toDTO(saved);
    }

    @Transactional
    public ApprovalWorkflowDTO auditWorkflow(Long workflowId, UserDetails user, WorkflowTransitionRequestDTO request) {
        ApprovalWorkflow workflow = getEntity(workflowId);
        ensureState(workflow, WorkflowState.EXECUTED, "Only executed workflow can be audited");
        ensureRole(user, UserRole.ADMIN);

        WorkflowState old = workflow.getState();
        workflow.setState(WorkflowState.AUDITED);
        workflow.setAuditedBy(actor(user));
        workflow.setAuditedAt(LocalDateTime.now());
        workflow.setAuditNote(request != null ? request.getNote() : null);

        ApprovalWorkflow saved = workflowRepository.save(workflow);
        audit(saved, "AUDIT", old, saved.getState(), saved.getOldValues(), saved.getNewValues(), actor(user), workflow.getAuditNote());
        return toDTO(saved);
    }

    private ApprovalWorkflow getEntity(Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Workflow not found with id: " + id));
    }

    private void ensureState(ApprovalWorkflow workflow, WorkflowState expected, String message) {
        if (workflow.getState() != expected) {
            throw new BadRequestException(message);
        }
    }

    private void ensureRole(UserDetails user, UserRole requiredRole) {
        if (requiredRole == null || user == null) {
            return;
        }
        String requiredAuthority = "ROLE_" + requiredRole.name();
        boolean ok = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(requiredAuthority::equals);
        if (!ok) {
            throw new BadRequestException("User does not have required role: " + requiredRole);
        }
    }

    private String actor(UserDetails user) {
        return user != null ? user.getUsername() : "system";
    }

    private void audit(
            ApprovalWorkflow workflow,
            String action,
            WorkflowState oldState,
            WorkflowState newState,
            String oldValues,
            String newValues,
            String changedBy,
            String note
    ) {
        auditTrailRepository.save(WorkflowAuditTrail.builder()
                .workflow(workflow)
                .action(action)
                .oldState(oldState)
                .newState(newState)
                .oldValues(oldValues)
                .newValues(newValues)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .note(note)
                .build());
    }

    private ApprovalWorkflowDTO toDTO(ApprovalWorkflow workflow) {
        return ApprovalWorkflowDTO.builder()
                .id(workflow.getId())
                .workflowType(workflow.getWorkflowType())
                .state(workflow.getState())
                .requiredApprovalRole(workflow.getRequiredApprovalRole())
                .targetEntity(workflow.getTargetEntity())
                .targetEntityId(workflow.getTargetEntityId())
                .title(workflow.getTitle())
                .description(workflow.getDescription())
                .oldValues(workflow.getOldValues())
                .newValues(workflow.getNewValues())
                .createdBy(workflow.getCreatedBy())
                .createdAt(workflow.getCreatedAt())
                .reviewedBy(workflow.getReviewedBy())
                .reviewedAt(workflow.getReviewedAt())
                .approvedBy(workflow.getApprovedBy())
                .approvedAt(workflow.getApprovedAt())
                .executedBy(workflow.getExecutedBy())
                .executedAt(workflow.getExecutedAt())
                .auditedBy(workflow.getAuditedBy())
                .auditedAt(workflow.getAuditedAt())
                .reviewComment(workflow.getReviewComment())
                .executionNote(workflow.getExecutionNote())
                .auditNote(workflow.getAuditNote())
                .build();
    }

    private WorkflowAuditTrailDTO toAuditDTO(WorkflowAuditTrail audit) {
        return WorkflowAuditTrailDTO.builder()
                .id(audit.getId())
                .workflowId(audit.getWorkflow() != null ? audit.getWorkflow().getId() : null)
                .action(audit.getAction())
                .oldState(audit.getOldState())
                .newState(audit.getNewState())
                .changedBy(audit.getChangedBy())
                .changedAt(audit.getChangedAt())
                .oldValues(audit.getOldValues())
                .newValues(audit.getNewValues())
                .note(audit.getNote())
                .build();
    }
}
