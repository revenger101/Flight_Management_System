package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDTO {
    private Long id;
    private NotificationEventType eventType;
    private NotificationChannel channel;
    private String name;
    private String bodyTemplate;
    private boolean active;
}
