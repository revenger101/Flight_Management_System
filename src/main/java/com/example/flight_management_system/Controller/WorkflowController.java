package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.ApprovalWorkflowDTO;
import com.example.flight_management_system.dto.WorkflowAuditTrailDTO;
import com.example.flight_management_system.dto.WorkflowTransitionRequestDTO;
import com.example.flight_management_system.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApprovalWorkflowDTO>> list() {
        return ResponseEntity.ok(workflowService.listWorkflows());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkflowAuditTrailDTO>> audit(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.auditTrail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> create(
            @RequestBody ApprovalWorkflowDTO dto,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(workflowService.createDraft(dto, user));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> submit(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowTransitionRequestDTO req,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(workflowService.submitForReview(id, user, req));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> approve(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowTransitionRequestDTO req,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(workflowService.approve(id, user, req));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> execute(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowTransitionRequestDTO req,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(workflowService.execute(id, user, req));
    }

    @PostMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalWorkflowDTO> markAudited(
            @PathVariable Long id,
            @RequestBody(required = false) WorkflowTransitionRequestDTO req,
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(workflowService.auditWorkflow(id, user, req));
    }
}
