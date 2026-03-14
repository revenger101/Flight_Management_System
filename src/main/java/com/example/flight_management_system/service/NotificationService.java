package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.NotificationEventDTO;
import com.example.flight_management_system.dto.NotificationTemplateDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.NotificationEvent;
import com.example.flight_management_system.entity.NotificationTemplate;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.repository.NotificationTemplateRepository;
import com.example.flight_management_system.repository.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;

    private NotificationEventDTO toDTO(NotificationEvent event) {
        return NotificationEventDTO.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .channel(event.getChannel())
                .message(event.getMessage())
                .createdAt(event.getCreatedAt())
                .flightId(event.getFlight() != null ? event.getFlight().getId() : null)
                .bookingId(event.getBooking() != null ? event.getBooking().getId() : null)
                .passengerId(event.getPassenger() != null ? event.getPassenger().getId() : null)
                .build();
    }

    public List<NotificationEventDTO> findAll() {
        return notificationEventRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationEventDTO> findByFlightId(Long flightId) {
        return notificationEventRepository.findByFlightIdOrderByCreatedAtDesc(flightId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationEventDTO> findByPassengerId(Long passengerId) {
        return notificationEventRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<NotificationTemplateDTO> listTemplates() {
        return notificationTemplateRepository.findAll().stream()
                .map(this::toTemplateDTO)
                .collect(Collectors.toList());
    }

    public NotificationTemplateDTO upsertTemplate(NotificationTemplateDTO dto) {
        NotificationTemplate entity = dto.getId() == null
                ? NotificationTemplate.builder().build()
                : notificationTemplateRepository.findById(dto.getId())
                .orElse(NotificationTemplate.builder().build());

        entity.setEventType(dto.getEventType());
        entity.setChannel(dto.getChannel());
        entity.setName(dto.getName());
        entity.setBodyTemplate(dto.getBodyTemplate());
        entity.setActive(dto.isActive());

        NotificationTemplate saved = notificationTemplateRepository.save(entity);
        return toTemplateDTO(saved);
    }

    public void notifyFlightEvent(Flight flight, NotificationEventType eventType, String message) {
        createForChannels(eventType, message, flight, null, null, defaultChannels(), null);
    }

    public void notifyBookingEvent(Booking booking, NotificationEventType eventType, String message) {
        createForChannels(eventType, message, booking.getFlight(), booking, booking.getPassenger(), defaultChannels(), buildContext(booking));
    }

    public void notifyBookingEvent(Booking booking, NotificationEventType eventType, String message, List<NotificationChannel> channels) {
        createForChannels(eventType, message, booking.getFlight(), booking, booking.getPassenger(), channels, buildContext(booking));
    }

    private List<NotificationChannel> defaultChannels() {
        return Arrays.asList(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.WHATSAPP,
                NotificationChannel.PUSH,
                NotificationChannel.WEBHOOK
        );
    }

    private void createForChannels(
            NotificationEventType eventType,
            String message,
            Flight flight,
            Booking booking,
            Passenger passenger,
            List<NotificationChannel> channels,
            Map<String, String> context
    ) {
        channels.forEach(channel -> notificationEventRepository.save(NotificationEvent.builder()
                .eventType(eventType)
                .channel(channel)
                .message(renderTemplateOrFallback(eventType, channel, message, context))
                .createdAt(LocalDateTime.now())
                .flight(flight)
                .booking(booking)
                .passenger(passenger)
                .build()));
    }

    private String renderTemplateOrFallback(
            NotificationEventType eventType,
            NotificationChannel channel,
            String fallback,
            Map<String, String> context
    ) {
        return notificationTemplateRepository.findFirstByEventTypeAndChannelAndActiveTrue(eventType, channel)
                .map(NotificationTemplate::getBodyTemplate)
                .map(template -> applyContext(template, context))
                .orElse(fallback);
    }

    private String applyContext(String template, Map<String, String> context) {
        if (template == null || context == null || context.isEmpty()) {
            return template;
        }
        String rendered = template;
        for (Map.Entry<String, String> entry : context.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";
            rendered = rendered.replace(token, entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
    }

    private Map<String, String> buildContext(Booking booking) {
        Map<String, String> context = new HashMap<>();
        if (booking == null) {
            return context;
        }

        context.put("bookingId", String.valueOf(booking.getId()));
        context.put("seat", booking.getSeatNumber() != null ? booking.getSeatNumber() : "TBA");
        context.put("status", booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN");
        context.put("fare", booking.getFinalFare() != null ? String.valueOf(booking.getFinalFare()) : "0");
        context.put("currency", booking.getCurrency() != null ? booking.getCurrency() : "USD");

        if (booking.getPassenger() != null) {
            context.put("passengerName", booking.getPassenger().getName());
        }

        if (booking.getFlight() != null) {
            context.put("flightId", String.valueOf(booking.getFlight().getId()));
            String dep = booking.getFlight().getDepartureAirport() != null
                    ? booking.getFlight().getDepartureAirport().getShortName() : "?";
            String arr = booking.getFlight().getArrivalAirport() != null
                    ? booking.getFlight().getArrivalAirport().getShortName() : "?";
            context.put("route", dep + " -> " + arr);
        }

        return context;
    }

    private NotificationTemplateDTO toTemplateDTO(NotificationTemplate template) {
        return NotificationTemplateDTO.builder()
                .id(template.getId())
                .eventType(template.getEventType())
                .channel(template.getChannel())
                .name(template.getName())
                .bodyTemplate(template.getBodyTemplate())
                .active(template.isActive())
                .build();
    }
}
