package com.example.flight_management_system.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundActionDTO {
    private Double amount;
    private String reason;
}
