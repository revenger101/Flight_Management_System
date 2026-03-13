package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.FlightDTO;
import com.example.flight_management_system.dto.FlightHandlingDTO;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.FlightHandling;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.AirportRepository;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
        private final BookingRepository bookingRepository;
        private final NotificationService notificationService;

    private FlightHandlingDTO toHandlingDTO(FlightHandling fh) {
        return FlightHandlingDTO.builder()
                .id(fh.getId())
                .boardingGate(fh.getBoardingGate())
                .delay(fh.getDelay())
                .date(fh.getDate())
                .time(fh.getTime())
                .build();
    }

    private FlightDTO toDTO(Flight flight) {
        long confirmed = bookingRepository.countByFlightIdAndStatusIn(
                flight.getId(),
                List.of(BookingStatus.CONFIRMED)
        );
        int availableSeats = Math.max(0, flight.getSeatCapacity() - (int) confirmed);

        return FlightDTO.builder()
                .id(flight.getId())
                .time(flight.getTime())
                .miles(flight.getMiles())
                .seatCapacity(flight.getSeatCapacity())
                .overbookingLimit(flight.getOverbookingLimit())
                .waitlistEnabled(flight.isWaitlistEnabled())
                .currentGate(flight.getCurrentGate())
                .delayMinutes(flight.getDelayMinutes())
                .status(flight.getStatus())
                .confirmedBookings((int) confirmed)
                .availableSeats(availableSeats)
                .departureAirportId(flight.getDepartureAirport() != null ? flight.getDepartureAirport().getId() : null)
                .arrivalAirportId(flight.getArrivalAirport() != null ? flight.getArrivalAirport().getId() : null)
                .flightHandlings(flight.getFlightHandlings() != null ?
                        flight.getFlightHandlings().stream().map(this::toHandlingDTO).collect(Collectors.toList())
                        : new ArrayList<>())
                .connectingFlightIds(flight.getConnectingFlights() != null ?
                        flight.getConnectingFlights().stream().map(Flight::getId).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    public List<FlightDTO> findAll() {
        return flightRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

        public Page<FlightDTO> search(FlightStatus status, Long departureAirportId, Long arrivalAirportId, Pageable pageable) {
                return flightRepository.search(status, departureAirportId, arrivalAirportId, pageable).map(this::toDTO);
        }

        public List<FlightDTO> findByStatus(FlightStatus status) {
                return flightRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
        }

    public FlightDTO findById(Long id) {
        return toDTO(flightRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + id)));
    }

        @Transactional
    public FlightDTO create(FlightDTO dto) {
        Airport departure = airportRepository.findById(dto.getDepartureAirportId())
                                .orElseThrow(() -> new NotFoundException("Departure airport not found"));
        Airport arrival = airportRepository.findById(dto.getArrivalAirportId())
                                .orElseThrow(() -> new NotFoundException("Arrival airport not found"));

        List<FlightHandling> handlings = new ArrayList<>();
        if (dto.getFlightHandlings() != null) {
            handlings = dto.getFlightHandlings().stream().map(fhDTO ->
                    FlightHandling.builder()
                            .boardingGate(fhDTO.getBoardingGate())
                            .delay(fhDTO.getDelay())
                            .date(fhDTO.getDate())
                            .time(fhDTO.getTime())
                            .build()
            ).collect(Collectors.toList());
        }

        Flight flight = Flight.builder()
                .time(dto.getTime())
                .miles(dto.getMiles())
                .seatCapacity(dto.getSeatCapacity() > 0 ? dto.getSeatCapacity() : 180)
                .overbookingLimit(Math.max(0, dto.getOverbookingLimit()))
                .waitlistEnabled(dto.isWaitlistEnabled())
                .currentGate(dto.getCurrentGate())
                .delayMinutes(Math.max(0, dto.getDelayMinutes()))
                .status(dto.getStatus() != null ? dto.getStatus() : FlightStatus.SCHEDULED)
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .flightHandlings(handlings)
                .build();

        return toDTO(flightRepository.save(flight));
    }

        @Transactional
    public FlightDTO update(Long id, FlightDTO dto) {
        Flight existing = flightRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + id));

        FlightStatus oldStatus = existing.getStatus();
        Integer oldGate = existing.getCurrentGate();

        existing.setTime(dto.getTime());
        existing.setMiles(dto.getMiles());
        existing.setSeatCapacity(dto.getSeatCapacity() > 0 ? dto.getSeatCapacity() : existing.getSeatCapacity());
        existing.setOverbookingLimit(dto.getOverbookingLimit() >= 0 ? dto.getOverbookingLimit() : existing.getOverbookingLimit());
        existing.setWaitlistEnabled(dto.isWaitlistEnabled());
        existing.setCurrentGate(dto.getCurrentGate());
        existing.setDelayMinutes(Math.max(0, dto.getDelayMinutes()));
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());

        if (dto.getDepartureAirportId() != null) {
            existing.setDepartureAirport(airportRepository.findById(dto.getDepartureAirportId())
                                        .orElseThrow(() -> new NotFoundException("Departure airport not found")));
        }
        if (dto.getArrivalAirportId() != null) {
            existing.setArrivalAirport(airportRepository.findById(dto.getArrivalAirportId())
                                        .orElseThrow(() -> new NotFoundException("Arrival airport not found")));
        }
                Flight saved = flightRepository.save(existing);

                if (oldStatus != saved.getStatus()) {
                        if (saved.getStatus() == FlightStatus.DELAYED) {
                                notificationService.notifyFlightEvent(saved, NotificationEventType.FLIGHT_DELAYED,
                                                "Flight #" + saved.getId() + " is delayed by " + saved.getDelayMinutes() + " minutes.");
                        }
                        if (saved.getStatus() == FlightStatus.CANCELLED) {
                                notificationService.notifyFlightEvent(saved, NotificationEventType.FLIGHT_CANCELLED,
                                                "Flight #" + saved.getId() + " has been cancelled.");
                        }
                }

                if (oldGate != null && saved.getCurrentGate() != null && !oldGate.equals(saved.getCurrentGate())) {
                        notificationService.notifyFlightEvent(saved, NotificationEventType.FLIGHT_GATE_CHANGED,
                                        "Flight #" + saved.getId() + " gate changed from " + oldGate + " to " + saved.getCurrentGate() + ".");
                }

                return toDTO(saved);
    }

    // Ajouter un vol connecté (ManyToMany)
    @Transactional
    public FlightDTO addConnectingFlight(Long flightId, Long connectedFlightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new NotFoundException("Flight not found"));
        Flight connected = flightRepository.findById(connectedFlightId)
                .orElseThrow(() -> new NotFoundException("Connected flight not found"));

        if (flight.getConnectingFlights() == null) flight.setConnectingFlights(new ArrayList<>());
        flight.getConnectingFlights().add(connected);
        return toDTO(flightRepository.save(flight));
    }

        @Transactional
    public void delete(Long id) {
        flightRepository.deleteById(id);
    }
}
