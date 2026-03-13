package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.LoyaltyLedgerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyLedgerDTO {
    private Long id;
    private LoyaltyLedgerType type;
    private int miles;
    private String note;
    private LocalDateTime createdAt;
    private Long passengerId;
    private Long bookingId;
}
