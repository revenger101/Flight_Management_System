package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.NotificationEventDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.NotificationEvent;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.repository.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationEventRepository notificationEventRepository;

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

    public void notifyFlightEvent(Flight flight, NotificationEventType eventType, String message) {
        createForChannels(eventType, message, flight, null, null, defaultChannels());
    }

    public void notifyBookingEvent(Booking booking, NotificationEventType eventType, String message) {
        createForChannels(eventType, message, booking.getFlight(), booking, booking.getPassenger(), defaultChannels());
    }

    private List<NotificationChannel> defaultChannels() {
        return Arrays.asList(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.WEBHOOK);
    }

    private void createForChannels(
            NotificationEventType eventType,
            String message,
            Flight flight,
            Booking booking,
            Passenger passenger,
            List<NotificationChannel> channels
    ) {
        channels.forEach(channel -> notificationEventRepository.save(NotificationEvent.builder()
                .eventType(eventType)
                .channel(channel)
                .message(message)
                .createdAt(LocalDateTime.now())
                .flight(flight)
                .booking(booking)
                .passenger(passenger)
                .build()));
    }
}
