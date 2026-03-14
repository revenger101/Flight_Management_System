package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.ServiceRequestStatus;
import com.example.flight_management_system.entity.enums.ServiceRequestType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequestDTO {
    private Long id;
    private ServiceRequestType type;
    private ServiceRequestStatus status;
    private String reason;
    private String resolution;
    private Double requestedAmount;
    private Double approvedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long bookingId;
    private Long passengerId;
}
