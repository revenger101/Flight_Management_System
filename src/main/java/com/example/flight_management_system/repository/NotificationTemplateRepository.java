package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.NotificationTemplate;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    List<NotificationTemplate> findByActiveTrueOrderByEventTypeAscChannelAsc();
    Optional<NotificationTemplate> findFirstByEventTypeAndChannelAndActiveTrue(NotificationEventType eventType, NotificationChannel channel);
}
