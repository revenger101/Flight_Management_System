package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventDTO {
    private Long id;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String message;
    private LocalDateTime createdAt;
    private Long flightId;
    private Long bookingId;
    private Long passengerId;
}
