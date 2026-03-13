package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.BookingDTO;
import com.example.flight_management_system.dto.RebookRequestDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;
    private final LoyaltyService loyaltyService;
    private final NotificationService notificationService;

    private BookingDTO toDTO(Booking b) {
        return BookingDTO.builder()
                .id(b.getId())
                .kind(b.getKind())
                .date(b.getDate())
                .type(b.getType())
                .status(b.getStatus())
                .cancellationReason(b.getCancellationReason())
                .rebookedToFlightId(b.getRebookedToFlightId())
                .passengerId(b.getPassenger() != null ? b.getPassenger().getId() : null)
                .flightId(b.getFlight() != null ? b.getFlight().getId() : null)
                .build();
    }

    public List<BookingDTO> findAll() {
        return bookingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public BookingDTO findById(Long id) {
        return toDTO(bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id)));
    }

    public List<BookingDTO> findByPassengerId(Long passengerId) {
        return bookingRepository.findByPassengerId(passengerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> findByFlightId(Long flightId) {
        return bookingRepository.findByFlightId(flightId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<BookingDTO> search(Long passengerId, Long flightId, BookingStatus status, Pageable pageable) {
        return bookingRepository.search(passengerId, flightId, status, pageable).map(this::toDTO);
    }

    @Transactional
    public BookingDTO create(BookingDTO dto, String idempotencyKey) {
        String normalizedIdempotencyKey = normalizeIdempotencyKey(idempotencyKey);

        if (normalizedIdempotencyKey != null) {
            return bookingRepository.findByIdempotencyKey(normalizedIdempotencyKey)
                    .map(this::toDTO)
                    .orElseGet(() -> createNewBooking(dto, normalizedIdempotencyKey));
        }

        return createNewBooking(dto, null);
    }

    private BookingDTO createNewBooking(BookingDTO dto, String idempotencyKey) {
        validateBookingRequest(dto);

        Passenger passenger = passengerRepository.findById(dto.getPassengerId())
                .orElseThrow(() -> new NotFoundException("Passenger not found with id: " + dto.getPassengerId()));
        Flight flight = flightRepository.findWithLockById(dto.getFlightId())
                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + dto.getFlightId()));

        BookingStatus bookingStatus = evaluateBookingStatus(flight);

        Booking booking = Booking.builder()
                .kind(dto.getKind())
                .date(dto.getDate())
                .type(dto.getType())
                .status(bookingStatus)
                .idempotencyKey(idempotencyKey)
                .passenger(passenger)
                .flight(flight)
                .build();

        Booking saved = bookingRepository.save(booking);
        if (saved.getStatus() == BookingStatus.CONFIRMED) {
            loyaltyService.accrueMiles(passenger, saved, flight.getMiles(), "Flight booking accrual");
            passengerRepository.save(passenger);
            notificationService.notifyBookingEvent(saved, NotificationEventType.BOOKING_CONFIRMED,
                    "Booking #" + saved.getId() + " is confirmed.");
        }
        return toDTO(saved);
    }

    @Transactional
    public BookingDTO update(Long id, BookingDTO dto) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        existing.setKind(dto.getKind());
        existing.setDate(dto.getDate());
        existing.setType(dto.getType());

        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        return toDTO(bookingRepository.save(existing));
    }

    @Transactional
    public BookingDTO cancel(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return toDTO(booking);
        }

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason != null ? reason : "Cancelled by operator");
        Booking saved = bookingRepository.save(booking);

        if (previousStatus == BookingStatus.CONFIRMED) {
            loyaltyService.reverseMiles(booking.getPassenger(), booking, booking.getFlight().getMiles(), "Booking cancellation reversal");
            passengerRepository.save(booking.getPassenger());
            promoteWaitlistedBooking(saved.getFlight());
        }

        notificationService.notifyBookingEvent(saved, NotificationEventType.BOOKING_CANCELLED,
                "Booking #" + saved.getId() + " has been cancelled.");

        return toDTO(saved);
    }

    @Transactional
    public BookingDTO rebook(Long id, RebookRequestDTO request) {
        Booking original = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + id));

        if (original.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot rebook a cancelled booking");
        }

        if (request == null || request.getNewFlightId() == null) {
            throw new BadRequestException("newFlightId is required for rebooking");
        }

        Flight newFlight = flightRepository.findWithLockById(request.getNewFlightId())
                .orElseThrow(() -> new NotFoundException("Target flight not found with id: " + request.getNewFlightId()));

        LocalDate newDate = request.getNewDate() != null ? request.getNewDate() : original.getDate();

        if (original.getStatus() == BookingStatus.CONFIRMED) {
            loyaltyService.reverseMiles(original.getPassenger(), original, original.getFlight().getMiles(), "Rebooking reversal");
            passengerRepository.save(original.getPassenger());
        }

        original.setStatus(BookingStatus.REBOOKED);
        original.setRebookedToFlightId(newFlight.getId());
        bookingRepository.save(original);

        BookingDTO newBookingDto = BookingDTO.builder()
                .kind(original.getKind())
                .date(newDate)
                .type(original.getType())
                .passengerId(original.getPassenger().getId())
                .flightId(newFlight.getId())
                .build();

        BookingDTO newBooking = create(newBookingDto, null);
        notificationService.notifyBookingEvent(original, NotificationEventType.BOOKING_REBOOKED,
                "Booking #" + original.getId() + " has been rebooked to flight #" + newFlight.getId() + ".");
        return newBooking;
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    private void validateBookingRequest(BookingDTO dto) {
        if (dto.getPassengerId() == null || dto.getFlightId() == null || dto.getDate() == null) {
            throw new BadRequestException("Passenger, flight and date are required");
        }

        if (dto.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Booking date cannot be in the past");
        }

        boolean hasSameDayBooking = bookingRepository.existsByPassengerIdAndDateAndStatusIn(
                dto.getPassengerId(),
                dto.getDate(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.WAITLISTED)
        );

        if (hasSameDayBooking) {
            throw new BadRequestException("Passenger already has an active booking on this date");
        }
    }

    private BookingStatus evaluateBookingStatus(Flight flight) {
        if (flight.getStatus() == FlightStatus.CANCELLED ||
                flight.getStatus() == FlightStatus.DEPARTED ||
                flight.getStatus() == FlightStatus.LANDED) {
            throw new BadRequestException("Flight is not open for new bookings in status: " + flight.getStatus());
        }

        long confirmedCount = bookingRepository.countByFlightIdAndStatusIn(
                flight.getId(),
                List.of(BookingStatus.CONFIRMED)
        );

        int maxSellable = Math.max(0, flight.getSeatCapacity() + flight.getOverbookingLimit());

        if (confirmedCount < maxSellable) {
            return BookingStatus.CONFIRMED;
        }

        if (flight.isWaitlistEnabled()) {
            return BookingStatus.WAITLISTED;
        }

        throw new BadRequestException("No inventory available for this flight");
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }

        String normalized = idempotencyKey.trim();
        if (normalized.length() > 80) {
            throw new BadRequestException("Idempotency-Key must be <= 80 characters");
        }
        return normalized;
    }

    private void promoteWaitlistedBooking(Flight flight) {
        bookingRepository.findFirstByFlightIdAndStatusOrderByIdAsc(flight.getId(), BookingStatus.WAITLISTED)
                .ifPresent(waitlisted -> {
                    waitlisted.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(waitlisted);
                    loyaltyService.accrueMiles(waitlisted.getPassenger(), waitlisted, flight.getMiles(), "Waitlist promotion accrual");
                    passengerRepository.save(waitlisted.getPassenger());
                    notificationService.notifyBookingEvent(waitlisted, NotificationEventType.WAITLIST_PROMOTED,
                            "Booking #" + waitlisted.getId() + " moved from waitlist to confirmed.");
                });
    }
}
