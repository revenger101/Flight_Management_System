package com.example.flight_management_system.entity;

import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private NotificationEventType eventType;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(length = 120)
    private String name;

    @Column(length = 2000)
    private String bodyTemplate;

    private boolean active;
}
