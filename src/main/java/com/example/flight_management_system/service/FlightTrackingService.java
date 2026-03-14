package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.LiveFlightTrackDTO;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightTrackingService {

    private final FlightRepository flightRepository;

    public List<LiveFlightTrackDTO> getLiveFlights() {
        LocalDateTime now = LocalDateTime.now();
        List<LiveFlightTrackDTO> result = new ArrayList<>();

        for (Flight flight : flightRepository.findAll()) {
            Airport dep = flight.getDepartureAirport();
            Airport arr = flight.getArrivalAirport();
            if (dep == null || arr == null || dep.getLatitude() == null || dep.getLongitude() == null
                    || arr.getLatitude() == null || arr.getLongitude() == null) {
                continue;
            }

            if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.LANDED) {
                continue;
            }

            LocalDateTime depTs = estimateDepartureTs(flight, now);
            LocalDateTime arrTs = estimateArrivalTs(flight, depTs);

            if (arrTs.isBefore(depTs)) {
                arrTs = depTs.plusMinutes(Math.max(45, flight.getMiles() / 8));
            }

            double progress = progress(now, depTs, arrTs);

            // Keep only flights likely airborne now, but allow boarding/departed around edges.
            boolean airborneWindow = progress > 0.03 && progress < 0.98;
            boolean eligibleStatus = flight.getStatus() == FlightStatus.DEPARTED
                    || flight.getStatus() == FlightStatus.DELAYED
                    || flight.getStatus() == FlightStatus.BOARDING
                    || flight.getStatus() == FlightStatus.SCHEDULED;

            if (!eligibleStatus || !airborneWindow) {
                continue;
            }

            double[] pos = greatCirclePoint(
                    dep.getLatitude(), dep.getLongitude(),
                    arr.getLatitude(), arr.getLongitude(),
                    progress
            );

            result.add(LiveFlightTrackDTO.builder()
                    .flightId(flight.getId())
                    .route(dep.getShortName() + " -> " + arr.getShortName())
                    .departureCode(dep.getShortName())
                    .arrivalCode(arr.getShortName())
                    .status(flight.getStatus())
                    .departureLat(dep.getLatitude())
                    .departureLon(dep.getLongitude())
                    .arrivalLat(arr.getLatitude())
                    .arrivalLon(arr.getLongitude())
                    .currentLat(pos[0])
                    .currentLon(pos[1])
                    .progress(round2(progress))
                    .altitudeFeet(simulatedAltitude(progress, flight.getStatus()))
                    .groundSpeedKts(simulatedSpeedKts(flight.getMiles(), progress))
                    .build());
        }

        if (result.isEmpty()) {
            // Fallback simulation if no flight is naturally in-air at current time.
            return buildFallbackSimulation(now);
        }

        return result.stream()
                .sorted(Comparator.comparing(LiveFlightTrackDTO::getFlightId))
                .toList();
    }

    private List<LiveFlightTrackDTO> buildFallbackSimulation(LocalDateTime now) {
        List<LiveFlightTrackDTO> fallback = new ArrayList<>();
        List<Flight> flights = flightRepository.findAll().stream()
                .filter(f -> f.getDepartureAirport() != null && f.getArrivalAirport() != null)
                .filter(f -> f.getDepartureAirport().getLatitude() != null && f.getArrivalAirport().getLatitude() != null)
                .filter(f -> f.getStatus() != FlightStatus.CANCELLED && f.getStatus() != FlightStatus.LANDED)
                .limit(12)
                .toList();

        int minute = now.getMinute();
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            Airport dep = f.getDepartureAirport();
            Airport arr = f.getArrivalAirport();
            double p = ((minute + i * 7) % 80 + 10) / 100.0;
            double[] pos = greatCirclePoint(dep.getLatitude(), dep.getLongitude(), arr.getLatitude(), arr.getLongitude(), p);

            fallback.add(LiveFlightTrackDTO.builder()
                    .flightId(f.getId())
                    .route(dep.getShortName() + " -> " + arr.getShortName())
                    .departureCode(dep.getShortName())
                    .arrivalCode(arr.getShortName())
                    .status(f.getStatus() == FlightStatus.SCHEDULED ? FlightStatus.DEPARTED : f.getStatus())
                    .departureLat(dep.getLatitude())
                    .departureLon(dep.getLongitude())
                    .arrivalLat(arr.getLatitude())
                    .arrivalLon(arr.getLongitude())
                    .currentLat(pos[0])
                    .currentLon(pos[1])
                    .progress(round2(p))
                    .altitudeFeet(simulatedAltitude(p, FlightStatus.DEPARTED))
                    .groundSpeedKts(simulatedSpeedKts(f.getMiles(), p))
                    .build());
        }

        return fallback;
    }

    private LocalDateTime estimateDepartureTs(Flight flight, LocalDateTime now) {
        if (flight.getScheduledDeparture() != null) {
            LocalDateTime dep = flight.getScheduledDeparture().plusMinutes(flight.getDelayMinutes());
            // normalize to nearest day so UI has live planes even when seeded for tomorrow.
            LocalDateTime aligned = dep.withYear(now.getYear()).withMonth(now.getMonthValue()).withDayOfMonth(now.getDayOfMonth());
            if (aligned.isAfter(now.plusHours(12))) aligned = aligned.minusDays(1);
            if (aligned.isBefore(now.minusHours(12))) aligned = aligned.plusDays(1);
            return aligned;
        }

        LocalTime base = flight.getTime() != null ? flight.getTime() : LocalTime.of(10, 0);
        return now.withHour(base.getHour()).withMinute(base.getMinute()).withSecond(0).withNano(0);
    }

    private LocalDateTime estimateArrivalTs(Flight flight, LocalDateTime depTs) {
        if (flight.getScheduledArrival() != null && flight.getScheduledDeparture() != null) {
            long mins = Duration.between(flight.getScheduledDeparture(), flight.getScheduledArrival()).toMinutes();
            if (mins > 0) {
                return depTs.plusMinutes(mins + flight.getDelayMinutes());
            }
        }
        return depTs.plusMinutes(Math.max(45, flight.getMiles() / 8));
    }

    private double progress(LocalDateTime now, LocalDateTime dep, LocalDateTime arr) {
        long total = Math.max(1, Duration.between(dep, arr).toSeconds());
        long elapsed = Duration.between(dep, now).toSeconds();
        return clamp(elapsed / (double) total, 0.0, 1.0);
    }

    private double[] greatCirclePoint(double lat1Deg, double lon1Deg, double lat2Deg, double lon2Deg, double t) {
        double lat1 = Math.toRadians(lat1Deg);
        double lon1 = Math.toRadians(lon1Deg);
        double lat2 = Math.toRadians(lat2Deg);
        double lon2 = Math.toRadians(lon2Deg);

        double[] p1 = toCartesian(lat1, lon1);
        double[] p2 = toCartesian(lat2, lon2);

        double omega = Math.acos(clamp(dot(p1, p2), -1.0, 1.0));
        if (omega < 1e-6) {
            return new double[]{lat1Deg, lon1Deg};
        }

        double sinOmega = Math.sin(omega);
        double a = Math.sin((1 - t) * omega) / sinOmega;
        double b = Math.sin(t * omega) / sinOmega;

        double x = a * p1[0] + b * p2[0];
        double y = a * p1[1] + b * p2[1];
        double z = a * p1[2] + b * p2[2];

        double norm = Math.sqrt(x * x + y * y + z * z);
        x /= norm;
        y /= norm;
        z /= norm;

        double lat = Math.toDegrees(Math.asin(z));
        double lon = Math.toDegrees(Math.atan2(y, x));
        return new double[]{lat, lon};
    }

    private double[] toCartesian(double lat, double lon) {
        double cosLat = Math.cos(lat);
        return new double[]{cosLat * Math.cos(lon), cosLat * Math.sin(lon), Math.sin(lat)};
    }

    private double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private int simulatedAltitude(double progress, FlightStatus status) {
        if (status == FlightStatus.BOARDING) return 0;
        if (progress < 0.1) return 6000 + (int) (progress * 120000);
        if (progress > 0.9) return 15000;
        return 33000 + (int) (Math.sin(progress * Math.PI) * 6000);
    }

    private int simulatedSpeedKts(int miles, double progress) {
        int base = miles > 3000 ? 480 : 430;
        int cruiseBoost = progress > 0.2 && progress < 0.8 ? 25 : -20;
        return Math.max(280, base + cruiseBoost);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
