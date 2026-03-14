package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.ServiceRequestStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceRequestStatusDTO {
    private ServiceRequestStatus status;
    private String resolution;
    private Double approvedAmount;
}
