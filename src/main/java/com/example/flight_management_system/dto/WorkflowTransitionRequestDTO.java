package com.example.flight_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransitionRequestDTO {
    private String note;
}
