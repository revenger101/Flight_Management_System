package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.BoardingPassDTO;
import com.example.flight_management_system.dto.PassengerPortalBookingDTO;
import com.example.flight_management_system.dto.ServiceRequestDTO;
import com.example.flight_management_system.dto.UpdateServiceRequestStatusDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.PassengerServiceRequest;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.entity.enums.ServiceRequestStatus;
import com.example.flight_management_system.entity.enums.ServiceRequestType;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.PassengerRepository;
import com.example.flight_management_system.repository.PassengerServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PassengerPortalService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final PassengerServiceRequestRepository requestRepository;
    private final NotificationService notificationService;

    public List<PassengerPortalBookingDTO> getPassengerBookings(Long passengerId) {
        ensurePassenger(passengerId);
        return bookingRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::toPortalBookingDTO)
                .toList();
    }

    @Transactional
    public PassengerPortalBookingDTO selectSeat(Long bookingId, String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            throw new BadRequestException("Seat number is required");
        }

        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot select seat for a cancelled booking");
        }

        booking.setSeatNumber(seatNumber.trim().toUpperCase());
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyBookingEvent(
                saved,
                NotificationEventType.SEAT_CHANGED,
                "Seat updated to " + saved.getSeatNumber() + " for booking #" + saved.getId(),
                List.of(NotificationChannel.EMAIL, NotificationChannel.SMS, NotificationChannel.PUSH)
        );

        return toPortalBookingDTO(saved);
    }

    @Transactional
    public BoardingPassDTO checkIn(Long bookingId) {
        Booking booking = getBooking(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.REBOOKED) {
            throw new BadRequestException("Only confirmed/rebooked bookings can be checked in");
        }

        if (booking.isCheckedIn()) {
            return toBoardingPassDTO(booking);
        }

        booking.setCheckedIn(true);
        booking.setCheckedInAt(LocalDateTime.now());
        if (booking.getSeatNumber() == null || booking.getSeatNumber().isBlank()) {
            booking.setSeatNumber("AUTO-" + (booking.getId() % 60 + 1));
        }
        booking.setBoardingPassCode("BP-" + booking.getId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Booking saved = bookingRepository.save(booking);

        notificationService.notifyBookingEvent(
                saved,
                NotificationEventType.CHECK_IN_COMPLETED,
                "Check-in completed for booking #" + saved.getId() + ". Boarding pass issued.",
                List.of(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP, NotificationChannel.PUSH)
        );

        return toBoardingPassDTO(saved);
    }

    public BoardingPassDTO getBoardingPass(Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (!booking.isCheckedIn() || booking.getBoardingPassCode() == null) {
            throw new BadRequestException("Booking is not checked in yet");
        }
        return toBoardingPassDTO(booking);
    }

    @Transactional
    public ServiceRequestDTO submitRefundRequest(Long bookingId, String reason) {
        Booking booking = getBooking(bookingId);

        if (!booking.isRefundable()) {
            throw new BadRequestException("This booking is non-refundable");
        }

        PassengerServiceRequest request = PassengerServiceRequest.builder()
                .type(ServiceRequestType.REFUND)
                .status(ServiceRequestStatus.SUBMITTED)
                .reason(reason != null && !reason.isBlank() ? reason : "Customer requested refund")
                .requestedAmount(booking.getFinalFare())
                .approvedAmount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .booking(booking)
                .passenger(booking.getPassenger())
                .build();

        PassengerServiceRequest saved = requestRepository.save(request);

        notificationService.notifyBookingEvent(
                booking,
                NotificationEventType.REFUND_REQUEST_SUBMITTED,
                "Refund request submitted for booking #" + booking.getId(),
                List.of(NotificationChannel.EMAIL, NotificationChannel.WHATSAPP)
        );

        return toRequestDTO(saved);
    }

    @Transactional
    public ServiceRequestDTO submitRebookRequest(Long bookingId, String reason) {
        Booking booking = getBooking(bookingId);

        PassengerServiceRequest request = PassengerServiceRequest.builder()
                .type(ServiceRequestType.REBOOK)
                .status(ServiceRequestStatus.SUBMITTED)
                .reason(reason != null && !reason.isBlank() ? reason : "Customer requested rebooking")
                .requestedAmount(0.0)
                .approvedAmount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .booking(booking)
                .passenger(booking.getPassenger())
                .build();

        PassengerServiceRequest saved = requestRepository.save(request);

        notificationService.notifyBookingEvent(
                booking,
                NotificationEventType.REBOOK_REQUEST_SUBMITTED,
                "Rebooking request submitted for booking #" + booking.getId(),
                List.of(NotificationChannel.EMAIL, NotificationChannel.SMS)
        );

        return toRequestDTO(saved);
    }

    public List<ServiceRequestDTO> getPassengerRequests(Long passengerId) {
        ensurePassenger(passengerId);
        return requestRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream()
                .map(this::toRequestDTO)
                .toList();
    }

    public ServiceRequestDTO getRequest(Long requestId) {
        PassengerServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Service request not found with id: " + requestId));
        return toRequestDTO(request);
    }

    @Transactional
    public ServiceRequestDTO updateRequestStatus(Long requestId, UpdateServiceRequestStatusDTO dto) {
        PassengerServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Service request not found with id: " + requestId));

        if (dto != null && dto.getStatus() != null) {
            request.setStatus(dto.getStatus());
        }
        if (dto != null) {
            request.setResolution(dto.getResolution());
            if (dto.getApprovedAmount() != null) {
                request.setApprovedAmount(dto.getApprovedAmount());
            }
        }
        request.setUpdatedAt(LocalDateTime.now());

        PassengerServiceRequest saved = requestRepository.save(request);
        return toRequestDTO(saved);
    }

    private Passenger ensurePassenger(Long passengerId) {
        return passengerRepository.findById(passengerId)
                .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + passengerId));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));
    }

    private PassengerPortalBookingDTO toPortalBookingDTO(Booking booking) {
        String dep = booking.getFlight() != null && booking.getFlight().getDepartureAirport() != null
                ? booking.getFlight().getDepartureAirport().getShortName() : "?";
        String arr = booking.getFlight() != null && booking.getFlight().getArrivalAirport() != null
                ? booking.getFlight().getArrivalAirport().getShortName() : "?";

        return PassengerPortalBookingDTO.builder()
                .bookingId(booking.getId())
                .passengerId(booking.getPassenger() != null ? booking.getPassenger().getId() : null)
                .passengerName(booking.getPassenger() != null ? booking.getPassenger().getName() : null)
                .flightId(booking.getFlight() != null ? booking.getFlight().getId() : null)
                .route(dep + " -> " + arr)
                .date(booking.getDate())
                .type(booking.getType())
                .status(booking.getStatus())
                .seatNumber(booking.getSeatNumber())
                .checkedIn(booking.isCheckedIn())
                .checkedInAt(booking.getCheckedInAt())
                .boardingPassCode(booking.getBoardingPassCode())
                .finalFare(booking.getFinalFare())
                .currency(booking.getCurrency())
                .build();
    }

    private BoardingPassDTO toBoardingPassDTO(Booking booking) {
        String dep = booking.getFlight() != null && booking.getFlight().getDepartureAirport() != null
                ? booking.getFlight().getDepartureAirport().getShortName() : "?";
        String arr = booking.getFlight() != null && booking.getFlight().getArrivalAirport() != null
                ? booking.getFlight().getArrivalAirport().getShortName() : "?";

        return BoardingPassDTO.builder()
                .bookingId(booking.getId())
                .passengerName(booking.getPassenger() != null ? booking.getPassenger().getName() : null)
                .route(dep + " -> " + arr)
                .seatNumber(booking.getSeatNumber())
                .gate(booking.getFlight() != null && booking.getFlight().getCurrentGate() != null
                        ? "G" + booking.getFlight().getCurrentGate() : "TBA")
                .departure(booking.getFlight() != null ? booking.getFlight().getScheduledDeparture() : null)
                .boardingPassCode(booking.getBoardingPassCode())
                .build();
    }

    private ServiceRequestDTO toRequestDTO(PassengerServiceRequest request) {
        return ServiceRequestDTO.builder()
                .id(request.getId())
                .type(request.getType())
                .status(request.getStatus())
                .reason(request.getReason())
                .resolution(request.getResolution())
                .requestedAmount(request.getRequestedAmount())
                .approvedAmount(request.getApprovedAmount())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .bookingId(request.getBooking() != null ? request.getBooking().getId() : null)
                .passengerId(request.getPassenger() != null ? request.getPassenger().getId() : null)
                .build();
    }
}
