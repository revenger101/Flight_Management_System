package com.example.flight_management_system.config;

import com.example.flight_management_system.entity.Aircraft;
import com.example.flight_management_system.entity.Airline;
import com.example.flight_management_system.entity.AppUser;
import com.example.flight_management_system.entity.Airport;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.CorporateRate;
import com.example.flight_management_system.entity.CrewMember;
import com.example.flight_management_system.entity.CrewRoster;
import com.example.flight_management_system.entity.FareRule;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.FlightHandling;
import com.example.flight_management_system.entity.GateSlot;
import com.example.flight_management_system.entity.LoyaltyLedger;
import com.example.flight_management_system.entity.MilesAccount;
import com.example.flight_management_system.entity.NotificationEvent;
import com.example.flight_management_system.entity.OperationalAlert;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.PricingCampaign;
import com.example.flight_management_system.entity.PromoCode;
import com.example.flight_management_system.entity.enums.AircraftStatus;
import com.example.flight_management_system.entity.enums.AlertSeverity;
import com.example.flight_management_system.entity.enums.AlertType;
import com.example.flight_management_system.entity.enums.AuthProvider;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.CrewRole;
import com.example.flight_management_system.entity.enums.DiscountType;
import com.example.flight_management_system.entity.enums.FlightStatus;
import com.example.flight_management_system.entity.enums.GateSlotStatus;
import com.example.flight_management_system.entity.enums.LoyaltyLedgerType;
import com.example.flight_management_system.entity.enums.NotificationChannel;
import com.example.flight_management_system.entity.enums.NotificationEventType;
import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.repository.AircraftRepository;
import com.example.flight_management_system.repository.AirlineRepository;
import com.example.flight_management_system.repository.AppUserRepository;
import com.example.flight_management_system.repository.AirportRepository;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.CorporateRateRepository;
import com.example.flight_management_system.repository.CrewMemberRepository;
import com.example.flight_management_system.repository.CrewRosterRepository;
import com.example.flight_management_system.repository.FareRuleRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.GateSlotRepository;
import com.example.flight_management_system.repository.LoyaltyLedgerRepository;
import com.example.flight_management_system.repository.MilesAccountRepository;
import com.example.flight_management_system.repository.NotificationEventRepository;
import com.example.flight_management_system.repository.OperationalAlertRepository;
import com.example.flight_management_system.repository.PassengerRepository;
import com.example.flight_management_system.repository.PricingCampaignRepository;
import com.example.flight_management_system.repository.PromoCodeRepository;
import com.example.flight_management_system.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final MilesAccountRepository milesAccountRepository;
    private final BookingRepository bookingRepository;
    private final LoyaltyLedgerRepository loyaltyLedgerRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FareRuleRepository fareRuleRepository;
    private final PricingCampaignRepository pricingCampaignRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final CorporateRateRepository corporateRateRepository;
    private final AircraftRepository aircraftRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRosterRepository crewRosterRepository;
    private final GateSlotRepository gateSlotRepository;
    private final OperationalAlertRepository operationalAlertRepository;

    @Value("${app.seed.demo.enabled:true}")
    private boolean demoSeedEnabled;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!demoSeedEnabled) {
            log.info("Dev demo seeding disabled");
            return;
        }

        resetTables();

        List<AppUser> users = seedUsers();
        List<Airline> airlines = seedAirlines();
        Map<String, Airport> airportsByCode = seedAirports(airlines);
        List<Aircraft> aircraft = seedAircraft();
        List<Flight> flights = seedFlights(airportsByCode, aircraft);
        linkConnectingFlights(flights);
        List<Passenger> passengers = seedPassengers();
        List<Booking> bookings = seedBookings(passengers, flights);
        List<LoyaltyLedger> loyaltyEntries = seedLoyaltyLedgers(bookings);
        List<NotificationEvent> notifications = seedNotifications(flights, bookings);
        int pricingConfigs = seedPricingConfigs(airportsByCode);
        List<CrewMember> crewMembers = seedCrewMembers();
        int rosterEntries = seedCrewRosters(crewMembers, flights);
        int gateSlots = seedGateSlots(flights, airportsByCode);
        int alerts = seedOperationalAlerts(flights);

        log.info(
            "Dev demo data loaded: {} users, {} airlines, {} airports, {} aircraft, {} flights, {} passengers, {} bookings, {} loyalty, {} notifications, {} pricing, {} crew, {} rosters, {} gate slots, {} alerts",
                users.size(), airlines.size(), airportsByCode.size(), aircraft.size(),
                flights.size(), passengers.size(), bookings.size(), loyaltyEntries.size(),
                notifications.size(), pricingConfigs, crewMembers.size(), rosterEntries, gateSlots, alerts
        );
        log.info("Demo credentials: admin@airport.local / Airport123! and analyst@airport.local / Airport123!");
    }

    private void resetTables() {
        List.of(
                "refresh_token",
                "notification_event",
                "loyalty_ledger",
                "booking",
                "corporate_rate",
                "promo_code",
                "pricing_campaign",
                "fare_rule",
                "operational_alert",
                "gate_slot",
                "crew_roster",
                "crew_member",
                "connecting_flights",
                "flight_handling",
                "flight",
                "passenger",
                "miles_account",
                "airport",
                "airline",
                "aircraft",
                "app_user"
        ).forEach(table -> jdbcTemplate.execute("DELETE FROM " + table));
    }

    private List<AppUser> seedUsers() {
        List<AppUser> users = List.of(
                AppUser.builder()
                        .fullName("Airport Administrator")
                        .email("admin@airport.local")
                        .passwordHash(passwordEncoder.encode("Airport123!"))
                        .role(UserRole.ADMIN)
                        .provider(AuthProvider.LOCAL)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .fullName("Operations Analyst")
                        .email("analyst@airport.local")
                        .passwordHash(passwordEncoder.encode("Airport123!"))
                        .role(UserRole.USER)
                        .provider(AuthProvider.LOCAL)
                        .enabled(true)
                        .build(),
                AppUser.builder()
                        .fullName("Google Supervisor")
                        .email("supervisor.google@airport.local")
                        .provider(AuthProvider.GOOGLE)
                        .providerId("google-seed-supervisor")
                        .role(UserRole.ADMIN)
                        .enabled(true)
                        .build()
        );
        return appUserRepository.saveAll(users);
    }

    private List<Airline> seedAirlines() {
        List<Airline> airlines = AIRLINE_SEEDS.stream()
                .map(seed -> Airline.builder()
                        .name(seed.name())
                        .shortName(seed.code())
                        .logo(seed.logo())
                        .build())
                .toList();
        return airlineRepository.saveAll(airlines);
    }

    private Map<String, Airport> seedAirports(List<Airline> airlines) {
        Map<String, Airline> airlinesByCode = new LinkedHashMap<>();
        for (Airline airline : airlines) {
            airlinesByCode.put(airline.getShortName(), airline);
        }

        List<Airport> airports = new ArrayList<>();
        for (AirportSeed seed : AIRPORT_SEEDS) {
            airports.add(Airport.builder()
                    .shortName(seed.code())
                    .name(seed.name())
                    .country(seed.country())
                    .fee(seed.fee())
                    .latitude(seed.latitude())
                    .longitude(seed.longitude())
                    .airline(seed.airlineCode() == null ? null : airlinesByCode.get(seed.airlineCode()))
                    .build());
        }

        Map<String, Airport> result = new LinkedHashMap<>();
        for (Airport airport : airportRepository.saveAll(airports)) {
            result.put(airport.getShortName(), airport);
        }
        return result;
    }

    private List<Aircraft> seedAircraft() {
        List<Aircraft> aircraft = List.of(
            Aircraft.builder().registration("CN-RGG").model("Boeing 737-800").manufacturer("Boeing")
                .totalSeats(189).economySeats(162).businessSeats(27).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(12400).airlineCode("RAM")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(45)).build(),
            Aircraft.builder().registration("F-GSPY").model("Airbus A320neo").manufacturer("Airbus")
                .totalSeats(180).economySeats(156).businessSeats(24).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(9800).airlineCode("AF")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(30)).build(),
            Aircraft.builder().registration("A6-EWD").model("Boeing 777-300ER").manufacturer("Boeing")
                .totalSeats(354).economySeats(302).businessSeats(52).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(21600).airlineCode("EK")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(12)).build(),
            Aircraft.builder().registration("TC-LJD").model("Airbus A321").manufacturer("Airbus")
                .totalSeats(220).economySeats(196).businessSeats(24).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(15200).airlineCode("TK")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(60)).build(),
            Aircraft.builder().registration("A7-BAF").model("Airbus A350-900").manufacturer("Airbus")
                .totalSeats(283).economySeats(247).businessSeats(36).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(8700).airlineCode("QR")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(90)).build(),
            Aircraft.builder().registration("D-AIWA").model("Airbus A380-800").manufacturer("Airbus")
                .totalSeats(509).economySeats(440).businessSeats(69).status(AircraftStatus.MAINTENANCE)
                .totalFlightHours(31200).airlineCode("LH")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(7)).build(),
            Aircraft.builder().registration("G-XLEA").model("Airbus A350-1000").manufacturer("Airbus")
                .totalSeats(331).economySeats(291).businessSeats(40).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(11400).airlineCode("BA")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(55)).build(),
            Aircraft.builder().registration("N381DN").model("Boeing 767-400ER").manufacturer("Boeing")
                .totalSeats(238).economySeats(205).businessSeats(33).status(AircraftStatus.AVAILABLE)
                .totalFlightHours(18900).airlineCode("DL")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(22)).build(),
            Aircraft.builder().registration("9V-SMB").model("Airbus A380-800").manufacturer("Airbus")
                .totalSeats(471).economySeats(409).businessSeats(62).status(AircraftStatus.IN_SERVICE)
                .totalFlightHours(24300).airlineCode("SQ")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(18)).build(),
            Aircraft.builder().registration("EI-ENG").model("Boeing 737-800").manufacturer("Boeing")
                .totalSeats(189).economySeats(175).businessSeats(14).status(AircraftStatus.AVAILABLE)
                .totalFlightHours(7600).airlineCode("FR")
                .nextMaintenanceAt(LocalDateTime.now().plusDays(75)).build()
        );
        return aircraftRepository.saveAll(aircraft);
    }

    private List<Flight> seedFlights(Map<String, Airport> airportsByCode, List<Aircraft> aircraft) {
        LocalDate baseDate = LocalDate.now().plusDays(1);
        List<Flight> flights = new ArrayList<>();

        // Aircraft assignment map: flight index → aircraft index (sparse)
        int[] aircraftAssignments = { 0, 1, 0, 3, 2, 1, 6, 9, 6, 4, 0, 3, 1, 0, 4, 7, 1, 8, 2, -1, 4, 7, -1, 8 };

        for (int index = 0; index < FLIGHT_SEEDS.size(); index++) {
            FlightSeed seed = FLIGHT_SEEDS.get(index);
            LocalTime depTime = LocalTime.parse(seed.departureTime());
            LocalDate flightDate = baseDate.plusDays(index % 6);
            LocalDateTime scheduledDep = LocalDateTime.of(flightDate, depTime);
            // Rough flight duration: miles / 8 minutes (≈480 mph)
            int flightDurationMin = Math.max(45, seed.miles() / 8);
            LocalDateTime scheduledArr = scheduledDep.plusMinutes(flightDurationMin);

            Aircraft assignedAircraft = null;
            if (index < aircraftAssignments.length && aircraftAssignments[index] >= 0
                    && aircraftAssignments[index] < aircraft.size()) {
                assignedAircraft = aircraft.get(aircraftAssignments[index]);
            }

            flights.add(Flight.builder()
                    .time(depTime)
                    .miles(seed.miles())
                    .seatCapacity(seed.seatCapacity())
                    .overbookingLimit(seed.overbookingLimit())
                    .waitlistEnabled(true)
                    .currentGate(seed.gate())
                    .delayMinutes(seed.delayMinutes())
                    .status(seed.status())
                    .scheduledDeparture(scheduledDep)
                    .scheduledArrival(scheduledArr)
                    .turnaroundMinutes(45)
                    .aircraft(assignedAircraft)
                    .departureAirport(airportsByCode.get(seed.departureCode()))
                    .arrivalAirport(airportsByCode.get(seed.arrivalCode()))
                    .flightHandlings(List.of(
                            FlightHandling.builder()
                                    .boardingGate(seed.gate())
                                    .delay(seed.delayMinutes())
                                    .date(flightDate)
                                    .time(depTime.minusMinutes(35))
                                    .build(),
                            FlightHandling.builder()
                                    .boardingGate(seed.gate() + 1)
                                    .delay(Math.max(seed.delayMinutes() - 5, 0))
                                    .date(flightDate.plusDays(7))
                                    .time(depTime.minusMinutes(40))
                                    .build()
                    ))
                    .connectingFlights(new ArrayList<>())
                    .build());
        }

        return flightRepository.saveAll(flights);
    }

    private List<CrewMember> seedCrewMembers() {
        List<CrewMember> crew = List.of(
            CrewMember.builder().name("Capt. Youssef Benkirane").role(CrewRole.CAPTAIN)
                .licenseNumber("ATPL-MA-0421").nationality("Moroccan")
                .dutyMinutesThisCycle(180).available(true).email("y.benkirane@crew.ram.ma").build(),
            CrewMember.builder().name("F/O Sara Hakim").role(CrewRole.FIRST_OFFICER)
                .licenseNumber("CPL-MA-1182").nationality("Moroccan")
                .dutyMinutesThisCycle(180).available(true).email("s.hakim@crew.ram.ma").build(),
            CrewMember.builder().name("Capt. Jean-Luc Marois").role(CrewRole.CAPTAIN)
                .licenseNumber("ATPL-FR-7734").nationality("French")
                .dutyMinutesThisCycle(300).available(true).email("jl.marois@crew.airfrance.fr").build(),
            CrewMember.builder().name("F/O Claire Dupuis").role(CrewRole.FIRST_OFFICER)
                .licenseNumber("CPL-FR-3312").nationality("French")
                .dutyMinutesThisCycle(240).available(true).email("c.dupuis@crew.airfrance.fr").build(),
            CrewMember.builder().name("Capt. Ahmed Al-Rashidi").role(CrewRole.CAPTAIN)
                .licenseNumber("ATPL-AE-0088").nationality("Emirati")
                .dutyMinutesThisCycle(120).available(true).email("a.alrashidi@crew.emirates.ae").build(),
            CrewMember.builder().name("F/O Omar Khalil").role(CrewRole.FIRST_OFFICER)
                .licenseNumber("CPL-AE-5543").nationality("Egyptian")
                .dutyMinutesThisCycle(120).available(true).email("o.khalil@crew.emirates.ae").build(),
            CrewMember.builder().name("Purser Leila Mesrobian").role(CrewRole.PURSER)
                .licenseNumber("CC-AE-2291").nationality("Armenian")
                .dutyMinutesThisCycle(180).available(true).email("l.mesrobian@crew.emirates.ae").build(),
            CrewMember.builder().name("Capt. Mehmet Yilmaz").role(CrewRole.CAPTAIN)
                .licenseNumber("ATPL-TR-3901").nationality("Turkish")
                .dutyMinutesThisCycle(90).available(true).email("m.yilmaz@crew.thy.com.tr").build(),
            CrewMember.builder().name("F/O Fatima Arslan").role(CrewRole.FIRST_OFFICER)
                .licenseNumber("CPL-TR-4412").nationality("Turkish")
                .dutyMinutesThisCycle(90).available(true).email("f.arslan@crew.thy.com.tr").build(),
            CrewMember.builder().name("Capt. David Shaw").role(CrewRole.CAPTAIN)
                .licenseNumber("ATPL-GB-8821").nationality("British")
                .dutyMinutesThisCycle(420).available(true).email("d.shaw@crew.ba.com").build(),
            CrewMember.builder().name("F/O Emma Clarke").role(CrewRole.FIRST_OFFICER)
                .licenseNumber("CPL-GB-5571").nationality("British")
                .dutyMinutesThisCycle(420).available(true).email("e.clarke@crew.ba.com").build(),
            CrewMember.builder().name("Cabin Crew Amira Zaoui").role(CrewRole.CABIN_CREW)
                .licenseNumber("CC-MA-4411").nationality("Moroccan")
                .dutyMinutesThisCycle(180).available(true).email("a.zaoui@crew.ram.ma").build(),
            CrewMember.builder().name("Cabin Crew Pierre Martin").role(CrewRole.CABIN_CREW)
                .licenseNumber("CC-FR-9921").nationality("French")
                .dutyMinutesThisCycle(300).available(true).email("p.martin@crew.airfrance.fr").build(),
            CrewMember.builder().name("Cabin Crew Nour Al-Sayed").role(CrewRole.CABIN_CREW)
                .licenseNumber("CC-AE-7761").nationality("Lebanese")
                .dutyMinutesThisCycle(120).available(false)
                .restPeriodEnd(LocalDateTime.now().plusHours(4))
                .email("n.alsayed@crew.emirates.ae").build(),
            CrewMember.builder().name("Cabin Crew Kenji Yamamoto").role(CrewRole.CABIN_CREW)
                .licenseNumber("CC-SG-3344").nationality("Japanese")
                .dutyMinutesThisCycle(60).available(true).email("k.yamamoto@crew.sq.com").build(),
            CrewMember.builder().name("Capt. Raj Patel").role(CrewRole.TRAINING_CAPTAIN)
                .licenseNumber("ATPL-SG-1122").nationality("Singaporean")
                .dutyMinutesThisCycle(240).available(true).email("r.patel@crew.sq.com").build(),
            CrewMember.builder().name("Purser Gabriel Santos").role(CrewRole.PURSER)
                .licenseNumber("CC-BR-8812").nationality("Brazilian")
                .dutyMinutesThisCycle(180).available(true).email("g.santos@crew.sq.com").build(),
            CrewMember.builder().name("Ground Crew Hassan El Amrani").role(CrewRole.GROUND_CREW)
                .licenseNumber("GC-MA-3301").nationality("Moroccan")
                .dutyMinutesThisCycle(240).available(true).email("h.elamrani@ground.cmn.ma").build(),
            CrewMember.builder().name("Ground Crew Sophie Laurent").role(CrewRole.GROUND_CREW)
                .licenseNumber("GC-FR-7712").nationality("French")
                .dutyMinutesThisCycle(480).available(false)
                .restPeriodEnd(LocalDateTime.now().plusHours(6))
                .email("s.laurent@ground.cdg.fr").build(),
            CrewMember.builder().name("Cabin Crew Hana Kovac").role(CrewRole.CABIN_CREW)
                .licenseNumber("CC-QR-5521").nationality("Croatian")
                .dutyMinutesThisCycle(90).available(true).email("h.kovac@crew.qatarairways.com.qa").build()
        );
        return crewMemberRepository.saveAll(crew);
    }

    private int seedCrewRosters(List<CrewMember> crew, List<Flight> flights) {
        if (crew.isEmpty() || flights.isEmpty()) return 0;
        List<CrewRoster> rosters = new ArrayList<>();

        // Flight 0 (CMN→CDG): Capt Benkirane, F/O Hakim, Cabin Amira
        addRoster(rosters, crew.get(0), flights.get(0), CrewRole.CAPTAIN,   true,  150);
        addRoster(rosters, crew.get(1), flights.get(0), CrewRole.FIRST_OFFICER, true, 150);
        addRoster(rosters, crew.get(11), flights.get(0), CrewRole.CABIN_CREW, true, 150);

        // Flight 1 (CDG→DXB): Capt Marois, F/O Dupuis, Cabin Pierre
        addRoster(rosters, crew.get(2), flights.get(1), CrewRole.CAPTAIN,   true,  390);
        addRoster(rosters, crew.get(3), flights.get(1), CrewRole.FIRST_OFFICER, true, 390);
        addRoster(rosters, crew.get(12), flights.get(1), CrewRole.CABIN_CREW, true, 390);

        // Flight 2 (CMN→IST): Capt Benkirane, F/O Hakim — NOT checked in (BOARDING alert trigger)
        addRoster(rosters, crew.get(0), flights.get(2), CrewRole.CAPTAIN,   false, 330);
        addRoster(rosters, crew.get(1), flights.get(2), CrewRole.FIRST_OFFICER, false, 330);

        // Flight 3 (IST→DXB DELAYED): Capt Yilmaz, F/O Arslan
        addRoster(rosters, crew.get(7), flights.get(3), CrewRole.CAPTAIN,   false, 222);
        addRoster(rosters, crew.get(8), flights.get(3), CrewRole.FIRST_OFFICER, false, 222);

        // Flight 4 (DXB→DOH): Capt Al-Rashidi, F/O Khalil, Purser Mesrobian
        addRoster(rosters, crew.get(4), flights.get(4), CrewRole.CAPTAIN,   true, 45);
        addRoster(rosters, crew.get(5), flights.get(4), CrewRole.FIRST_OFFICER, true, 45);
        addRoster(rosters, crew.get(6), flights.get(4), CrewRole.PURSER,    true, 45);

        // Flight 8 (LHR→IST, DEPARTED): Capt Shaw, F/O Clarke
        addRoster(rosters, crew.get(9),  flights.get(8), CrewRole.CAPTAIN,  true, 310);
        addRoster(rosters, crew.get(10), flights.get(8), CrewRole.FIRST_OFFICER, true, 310);

        // Flight 17 (AMS→SIN): Capt Patel (training), F/O Clarke, Purser Santos, Cabin Hana
        addRoster(rosters, crew.get(15), flights.get(17), CrewRole.TRAINING_CAPTAIN, false, 800);
        addRoster(rosters, crew.get(10), flights.get(17), CrewRole.FIRST_OFFICER, false, 800);
        addRoster(rosters, crew.get(16), flights.get(17), CrewRole.PURSER,  false, 800);
        addRoster(rosters, crew.get(19), flights.get(17), CrewRole.CABIN_CREW, false, 800);

        crewRosterRepository.saveAll(rosters);
        return rosters.size();
    }

    private void addRoster(List<CrewRoster> list, CrewMember cm, Flight flight,
                            CrewRole role, boolean checkedIn, int dutyMin) {
        list.add(CrewRoster.builder()
                .crewMember(cm)
                .flight(flight)
                .roleOnFlight(role)
                .checkedIn(checkedIn)
                .checkedInAt(checkedIn ? LocalDateTime.now().minusMinutes(30) : null)
                .estimatedDutyMinutes(dutyMin)
                .dutyTimeCompliant(cm.getDutyMinutesThisCycle() + dutyMin <= cm.getMaxDutyMinutesPerCycle())
                .restRuleCompliant(cm.getRestPeriodEnd() == null
                        || LocalDateTime.now().isAfter(cm.getRestPeriodEnd()))
                .assignedAt(LocalDateTime.now().minusHours(12))
                .build());
    }

    private int seedGateSlots(List<Flight> flights, Map<String, Airport> airportsByCode) {
        List<GateSlot> slots = new ArrayList<>();
        for (int i = 0; i < Math.min(flights.size(), 18); i++) {
            Flight f = flights.get(i);
            if (f.getDepartureAirport() == null || f.getScheduledDeparture() == null) continue;
            LocalDateTime start = f.getScheduledDeparture().minusMinutes(75);
            LocalDateTime end   = f.getScheduledDeparture().plusMinutes(f.getDelayMinutes());
            slots.add(GateSlot.builder()
                    .airport(f.getDepartureAirport())
                    .gateNumber(f.getCurrentGate() != null ? f.getCurrentGate() : (i + 1))
                    .flight(f)
                    .scheduledStart(start)
                    .scheduledEnd(end)
                    .status(gateSlotStatusFor(f.getStatus()))
                    .build());
        }
        // Intentional gate conflict: same airport, same gate, overlapping window (flights 0 + 11)
        if (flights.size() > 11 && flights.get(0).getDepartureAirport() != null
                && flights.get(11).getArrivalAirport() != null) {
            Flight f0 = flights.get(0);
            Flight f11 = flights.get(11);
            if (f0.getDepartureAirport().getId().equals(f11.getArrivalAirport() != null
                    ? f11.getArrivalAirport().getId() : -1L)) {
                // skip – different airports; conflict only meaningful at same airport
            } else {
                // Add a conflict slot at CMN gate 12 for demo purposes
                if (f0.getScheduledDeparture() != null) {
                    GateSlot conflict = GateSlot.builder()
                            .airport(f0.getDepartureAirport())
                            .gateNumber(12)
                            .flight(f0)
                            .scheduledStart(f0.getScheduledDeparture().minusMinutes(30))
                            .scheduledEnd(f0.getScheduledDeparture().plusMinutes(10))
                            .status(GateSlotStatus.SCHEDULED)
                            .conflict(false)
                            .build();
                    slots.add(conflict);
                }
            }
        }
        gateSlotRepository.saveAll(slots);
        return slots.size();
    }

    private GateSlotStatus gateSlotStatusFor(FlightStatus fs) {
        return switch (fs) {
            case BOARDING  -> GateSlotStatus.ACTIVE;
            case DEPARTED, LANDED -> GateSlotStatus.COMPLETED;
            case CANCELLED -> GateSlotStatus.CANCELLED;
            default        -> GateSlotStatus.SCHEDULED;
        };
    }

    private int seedOperationalAlerts(List<Flight> flights) {
        List<OperationalAlert> alerts = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getStatus() == FlightStatus.DELAYED && f.getDelayMinutes() >= 30) {
                alerts.add(OperationalAlert.builder()
                        .alertType(AlertType.BOARDING_DELAY)
                        .severity(f.getDelayMinutes() >= 60 ? AlertSeverity.HIGH : AlertSeverity.MEDIUM)
                        .flight(f)
                        .message("Boarding delay on flight #" + f.getId() + " (" + routeLabel(f) + ")")
                        .details("Current delay: " + f.getDelayMinutes() + " min. Check gate readiness and crew check-in status.")
                        .triggeredAt(LocalDateTime.now().minusMinutes(20))
                        .slaDeadline(LocalDateTime.now().plusMinutes(25))
                        .resolved(false)
                        .build());
            }
            if (f.getStatus() == FlightStatus.BOARDING) {
                alerts.add(OperationalAlert.builder()
                        .alertType(AlertType.CREW_NOT_CHECKED_IN)
                        .severity(AlertSeverity.HIGH)
                        .flight(f)
                        .message("Crew check-in incomplete for boarding flight #" + f.getId())
                        .details("Flight is in BOARDING status but not all assigned crew have checked in.")
                        .triggeredAt(LocalDateTime.now().minusMinutes(10))
                        .slaDeadline(LocalDateTime.now().plusMinutes(15))
                        .resolved(false)
                        .build());
            }
        }
        // Add a fueling alert for a specific flight
        if (!flights.isEmpty()) {
            Flight f = flights.get(0);
            alerts.add(OperationalAlert.builder()
                    .alertType(AlertType.FUELING_DELAY)
                    .severity(AlertSeverity.MEDIUM)
                    .flight(f)
                    .message("Fueling crew not yet at gate for flight #" + f.getId())
                    .details("Fueling SLA: 90 min before departure. Currently 75 min to departure.")
                    .triggeredAt(LocalDateTime.now().minusMinutes(5))
                    .slaDeadline(LocalDateTime.now().plusMinutes(15))
                    .resolved(false)
                    .build());
        }
        operationalAlertRepository.saveAll(alerts);
        return alerts.size();
    }

    private static String routeLabel(Flight f) {
        String dep = f.getDepartureAirport() != null ? f.getDepartureAirport().getShortName() : "?";
        String arr = f.getArrivalAirport() != null ? f.getArrivalAirport().getShortName() : "?";
        return dep + " → " + arr;
    }

    private void linkConnectingFlights(List<Flight> flights) {
        connect(flights, 0, 1);
        connect(flights, 0, 5);
        connect(flights, 2, 3);
        connect(flights, 4, 9);
        connect(flights, 6, 7);
        connect(flights, 8, 13);
        connect(flights, 10, 11);
        connect(flights, 12, 15);
        connect(flights, 14, 16);
        connect(flights, 18, 19);
        flightRepository.saveAll(flights);
    }

    private List<Passenger> seedPassengers() {
        List<Passenger> passengers = new ArrayList<>();
        for (int index = 0; index < PASSENGER_NAMES.size(); index++) {
            int flightMiles = 7_500 + (index * 4_600);
            passengers.add(Passenger.builder()
                    .name(PASSENGER_NAMES.get(index))
                    .cc(String.format("ID-%05d", index + 1))
                    .mileCard(String.format("MC-%04d", 1000 + index))
                    .status(tierForMiles(flightMiles))
                    .milesAccount(MilesAccount.builder()
                            .number(String.format("MA-%04d", 2000 + index))
                            .flightMiles(flightMiles)
                            .statusMiles(Math.max(flightMiles / 3, 500))
                            .build())
                    .build());
        }

        List<Passenger> saved = passengerRepository.saveAll(passengers);
        milesAccountRepository.flush();
        return saved;
    }

    private List<Booking> seedBookings(List<Passenger> passengers, List<Flight> flights) {
        LocalDate baseDate = LocalDate.now().plusDays(1);
        List<Booking> bookings = new ArrayList<>();

        for (int passengerIndex = 0; passengerIndex < passengers.size(); passengerIndex++) {
            Passenger passenger = passengers.get(passengerIndex);
            for (int slot = 0; slot < 2; slot++) {
                int bookingIndex = (passengerIndex * 2) + slot;
                Flight flight = flights.get((passengerIndex * 3 + slot * 5) % flights.size());
                BookingStatus status = bookingStatusFor(bookingIndex);
                Flight alternativeFlight = flights.get((bookingIndex + 4) % flights.size());

                Booking booking = Booking.builder()
                        .kind(slot == 0 ? "One-way" : (bookingIndex % 3 == 0 ? "Connecting" : "Round-trip"))
                        .date(baseDate.plusDays(bookingIndex % 14))
                        .type(bookingIndex % 4 == 0 ? BookingType.BUSINESS : BookingType.ECONOMIC)
                        .status(status)
                        .idempotencyKey("seed-booking-" + (bookingIndex + 1))
                        .cancellationReason(status == BookingStatus.CANCELLED ? "Seeded cancellation for demo workflow" : null)
                        .rebookedToFlightId(status == BookingStatus.REBOOKED ? alternativeFlight.getId() : null)
                        .passenger(passenger)
                        .flight(flight)
                        .build();
                bookings.add(booking);
            }
        }

        return bookingRepository.saveAll(bookings);
    }

    private List<LoyaltyLedger> seedLoyaltyLedgers(List<Booking> bookings) {
        List<LoyaltyLedger> entries = new ArrayList<>();

        for (int index = 0; index < bookings.size(); index++) {
            Booking booking = bookings.get(index);
            int miles = Math.max(booking.getFlight().getMiles() / 2, 250);

            if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.REBOOKED) {
                entries.add(LoyaltyLedger.builder()
                        .type(LoyaltyLedgerType.ACCRUAL)
                        .miles(miles)
                        .note("Seed accrual for booking #" + booking.getId())
                        .createdAt(LocalDateTime.now().minusDays(index % 9L))
                        .passenger(booking.getPassenger())
                        .booking(booking)
                        .build());
            } else if (booking.getStatus() == BookingStatus.CANCELLED) {
                entries.add(LoyaltyLedger.builder()
                        .type(LoyaltyLedgerType.REVERSAL)
                        .miles(-Math.max(miles / 2, 150))
                        .note("Seed reversal for cancelled booking #" + booking.getId())
                        .createdAt(LocalDateTime.now().minusDays(index % 7L))
                        .passenger(booking.getPassenger())
                        .booking(booking)
                        .build());
            }
        }

        return loyaltyLedgerRepository.saveAll(entries);
    }

    private List<NotificationEvent> seedNotifications(List<Flight> flights, List<Booking> bookings) {
        List<NotificationEvent> events = new ArrayList<>();

        for (int index = 0; index < flights.size(); index++) {
            Flight flight = flights.get(index);
            NotificationEventType eventType = null;

            if (flight.getStatus() == FlightStatus.DELAYED) {
                eventType = NotificationEventType.FLIGHT_DELAYED;
            } else if (flight.getStatus() == FlightStatus.CANCELLED) {
                eventType = NotificationEventType.FLIGHT_CANCELLED;
            } else if (flight.getStatus() == FlightStatus.BOARDING) {
                eventType = NotificationEventType.FLIGHT_GATE_CHANGED;
            }

            if (eventType != null) {
                events.add(NotificationEvent.builder()
                        .eventType(eventType)
                        .channel(channelFor(index))
                        .message("Operational update for flight " + flight.getId() + " from "
                                + flight.getDepartureAirport().getShortName() + " to " + flight.getArrivalAirport().getShortName())
                        .createdAt(LocalDateTime.now().minusHours(index + 1L))
                        .flight(flight)
                        .build());
            }
        }

        for (int index = 0; index < bookings.size(); index++) {
            Booking booking = bookings.get(index);
            events.add(NotificationEvent.builder()
                    .eventType(notificationTypeFor(booking.getStatus()))
                    .channel(channelFor(index + 3))
                    .message(notificationMessageFor(booking))
                    .createdAt(LocalDateTime.now().minusHours((index % 18L) + 2))
                    .flight(booking.getFlight())
                    .booking(booking)
                    .passenger(booking.getPassenger())
                    .build());
        }

        return notificationEventRepository.saveAll(events);
    }

        private int seedPricingConfigs(Map<String, Airport> airportsByCode) {
        FareRule economyDefault = FareRule.builder()
            .bookingType(BookingType.ECONOMIC)
            .baseFareMultiplier(1.0)
            .refundable(false)
            .changeFee(85.0)
            .includedBaggageKg(20)
            .extraBaggageFeePerKg(7.0)
            .currency("USD")
            .build();

        FareRule businessDefault = FareRule.builder()
            .bookingType(BookingType.BUSINESS)
            .baseFareMultiplier(1.15)
            .refundable(true)
            .changeFee(25.0)
            .includedBaggageKg(35)
            .extraBaggageFeePerKg(5.5)
            .currency("USD")
            .build();

        FareRule cmnCdgEconomic = FareRule.builder()
            .departureAirport(airportsByCode.get("CMN"))
            .arrivalAirport(airportsByCode.get("CDG"))
            .bookingType(BookingType.ECONOMIC)
            .baseFare(249.0)
            .baseFareMultiplier(1.0)
            .refundable(false)
            .changeFee(95.0)
            .includedBaggageKg(23)
            .extraBaggageFeePerKg(8.0)
            .currency("USD")
            .build();

        fareRuleRepository.saveAll(List.of(economyDefault, businessDefault, cmnCdgEconomic));

        PricingCampaign summerCampaign = PricingCampaign.builder()
            .name("Summer Demand Booster")
            .description("Seasonal campaign for selected long-haul routes")
            .discountType(DiscountType.PERCENTAGE)
            .discountValue(8.0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(7))
            .endsAt(LocalDateTime.now().plusMonths(2))
            .departureAirport(airportsByCode.get("CDG"))
            .bookingType(BookingType.BUSINESS)
            .build();

        PricingCampaign launchCampaign = PricingCampaign.builder()
            .name("Network Expansion Promo")
            .description("Applies to all fare classes")
            .discountType(DiscountType.FIXED)
            .discountValue(35.0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(1))
            .endsAt(LocalDateTime.now().plusMonths(1))
            .build();

        pricingCampaignRepository.saveAll(List.of(summerCampaign, launchCampaign));

        PromoCode promoCode = PromoCode.builder()
            .code("WELCOME25")
            .description("Welcome offer")
            .discountType(DiscountType.PERCENTAGE)
            .discountValue(12.0)
            .minSubtotal(180.0)
            .maxUses(500)
            .usedCount(0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(2))
            .endsAt(LocalDateTime.now().plusMonths(6))
            .build();

        PromoCode premiumPromoCode = PromoCode.builder()
            .code("BUSI120")
            .description("Business class fixed discount")
            .discountType(DiscountType.FIXED)
            .discountValue(120.0)
            .minSubtotal(900.0)
            .maxUses(150)
            .usedCount(0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(2))
            .endsAt(LocalDateTime.now().plusMonths(6))
            .build();

        promoCodeRepository.saveAll(List.of(promoCode, premiumPromoCode));

        CorporateRate atlasRate = CorporateRate.builder()
            .corporateCode("ATLASCORP")
            .companyName("Atlas Corporate Travel")
            .discountPercent(14.0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(1))
            .endsAt(LocalDateTime.now().plusYears(1))
            .build();

        CorporateRate medLogRate = CorporateRate.builder()
            .corporateCode("MEDLOG")
            .companyName("Mediterranean Logistics Group")
            .discountPercent(9.0)
            .active(true)
            .startsAt(LocalDateTime.now().minusDays(1))
            .endsAt(LocalDateTime.now().plusYears(1))
            .departureAirport(airportsByCode.get("CMN"))
            .bookingType(BookingType.BUSINESS)
            .build();

        corporateRateRepository.saveAll(List.of(atlasRate, medLogRate));

        return 9;
        }

    private void connect(List<Flight> flights, int sourceIndex, int targetIndex) {
        flights.get(sourceIndex).getConnectingFlights().add(flights.get(targetIndex));
    }

    private String tierForMiles(int flightMiles) {
        if (flightMiles >= 110_000) {
            return "Platinum";
        }
        if (flightMiles >= 65_000) {
            return "Gold";
        }
        if (flightMiles >= 25_000) {
            return "Silver";
        }
        return "Bronze";
    }

    private BookingStatus bookingStatusFor(int bookingIndex) {
        if (bookingIndex % 11 == 0) {
            return BookingStatus.WAITLISTED;
        }
        if (bookingIndex % 13 == 0) {
            return BookingStatus.CANCELLED;
        }
        if (bookingIndex % 17 == 0) {
            return BookingStatus.REBOOKED;
        }
        return BookingStatus.CONFIRMED;
    }

    private NotificationChannel channelFor(int index) {
        NotificationChannel[] channels = NotificationChannel.values();
        return channels[index % channels.length];
    }

    private NotificationEventType notificationTypeFor(BookingStatus status) {
        return switch (status) {
            case CANCELLED -> NotificationEventType.BOOKING_CANCELLED;
            case REBOOKED -> NotificationEventType.BOOKING_REBOOKED;
            case WAITLISTED -> NotificationEventType.WAITLIST_PROMOTED;
            case CONFIRMED -> NotificationEventType.BOOKING_CONFIRMED;
        };
    }

    private String notificationMessageFor(Booking booking) {
        return switch (booking.getStatus()) {
            case CANCELLED -> "Booking #" + booking.getId() + " was cancelled";
            case REBOOKED -> "Booking #" + booking.getId() + " was rebooked to flight #" + booking.getRebookedToFlightId();
            case WAITLISTED -> "Booking #" + booking.getId() + " is currently managed through the waitlist workflow";
            case CONFIRMED -> "Booking #" + booking.getId() + " is confirmed for "
                    + booking.getFlight().getDepartureAirport().getShortName() + " to "
                    + booking.getFlight().getArrivalAirport().getShortName();
        };
    }

    private record AirlineSeed(String name, String code, String logo) { }

    private record AirportSeed(String code, String name, String country, float fee, Double latitude, Double longitude, String airlineCode) { }

    private record FlightSeed(
            String departureCode,
            String arrivalCode,
            String departureTime,
            int miles,
            int seatCapacity,
            int overbookingLimit,
            int gate,
            int delayMinutes,
            FlightStatus status
    ) { }

    private static final List<AirlineSeed> AIRLINE_SEEDS = List.of(
            new AirlineSeed("Royal Air Maroc", "RAM", "https://play-lh.googleusercontent.com/VpqfdMhLvOYnDOy2-wAe29eX3HsPNh5Hc4X7z2KUq1FxKhx4xREWAXDc0HsNapfJly8=w240-h480-rw"),
            new AirlineSeed("Air France", "AF", "https://yt3.googleusercontent.com/_MK6C-ccTQydVRLs6nRv_gNEdhFXRbTiW_oihFCbPwo_BWWl6-Ia5ws5G9y7FsSFbeKP94fpKw=s900-c-k-c0x00ffffff-no-rj"),
            new AirlineSeed("Emirates", "EK", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Emirates_logo.svg/1280px-Emirates_logo.svg.png"),
            new AirlineSeed("Turkish Airlines", "TK", "https://images.seeklogo.com/logo-png/50/2/turkish-airlines-logo-png_seeklogo-502732.png"),
            new AirlineSeed("Qatar Airways", "QR", "https://www.citypng.com/public/uploads/preview/qatar-airways-black-logo-transparent-background-7017516947105003qjqpogyd8.png"),
            new AirlineSeed("Lufthansa", "LH", "https://1000logos.net/wp-content/uploads/2017/03/Lufthansa-symbol.jpg"),
            new AirlineSeed("British Airways", "BA", "https://i.pinimg.com/564x/49/57/07/4957072a43937ac100d9e2052fc95d70.jpg"),
            new AirlineSeed("Ryanair", "FR", "https://download.logo.wine/logo/Ryanair/Ryanair-Logo.wine.png"),
            new AirlineSeed("Delta Air Lines", "DL", "https://wallpapercat.com/w/full/6/6/f/1449277-3840x2160-desktop-4k-delta-air-lines-wallpaper-image.jpg"),
            new AirlineSeed("Singapore Airlines", "SQ", "https://cdn.freebiesupply.com/logos/large/2x/singapore-airlines-logo-png-transparent.png"),
            new AirlineSeed("Air Arabia", "AA", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/88/Air_Arabia_Logo.svg/500px-Air_Arabia_Logo.svg.png"),
            new AirlineSeed("Air India", "AI", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bf/Air_India_2023.svg/1280px-Air_India_2023.svg.png"),
            new AirlineSeed("Air Europa", "AE", "https://static.cdnlogo.com/logos/a/79/air-europa.png"),
            new AirlineSeed("Air China", "CA", "https://download.logo.wine/logo/Air_China/Air_China-Logo.wine.png"),
            new AirlineSeed("Air Canada", "AC", "https://logos-world.net/wp-content/uploads/2021/05/Air-Canada-Logo.png"),
            new AirlineSeed("Air New Zealand", "ANZ", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Air_NewZealand-Logo.svg/960px-Air_NewZealand-Logo.svg.png"),
            new AirlineSeed("Air India Express", "IX", "https://brandlogos.net/wp-content/uploads/2014/12/air_india_express-logo_brandlogos.net_bwubm-512x512.png"),
            new AirlineSeed("Air Austral", "AAU", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQVX_XG3fR12W7lkUypseBqokafUfRvkYBKHQ&s"),
            new AirlineSeed("Air Serbia", "JU", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT19IAIZ7tiyVDFLan8Mo_XeU8Pg-3DwP2h3Q&s"),
            new AirlineSeed("Air Baltic", "BT", "https://upload.wikimedia.org/wikipedia/commons/7/79/Airbaltic-logo.png"),
            new AirlineSeed("Tunisair", "TU", "https://i.pinimg.com/474x/0d/17/12/0d1712f29b0ac668485449c5b478fc38.jpg")

    );

    private static final List<AirportSeed> AIRPORT_SEEDS = List.of(
            new AirportSeed("CMN", "Mohammed V International Airport", "Morocco", 45.0f, 33.3675, -7.5899, "RAM"),
            new AirportSeed("CDG", "Charles de Gaulle Airport", "France", 65.0f, 49.0097, 2.5479, "AF"),
            new AirportSeed("DXB", "Dubai International Airport", "United Arab Emirates", 55.0f, 25.2532, 55.3657, "EK"),
            new AirportSeed("IST", "Istanbul Airport", "Turkey", 40.0f, 41.2753, 28.7519, "TK"),
            new AirportSeed("DOH", "Hamad International Airport", "Qatar", 50.0f, 25.2731, 51.6080, "QR"),
            new AirportSeed("FRA", "Frankfurt Airport", "Germany", 60.0f, 50.0379, 8.5622, "LH"),
            new AirportSeed("LHR", "London Heathrow Airport", "United Kingdom", 75.0f, 51.4700, -0.4543, "BA"),
            new AirportSeed("RAK", "Marrakech Menara Airport", "Morocco", 35.0f, 31.6069, -8.0363, "FR"),
            new AirportSeed("JFK", "John F. Kennedy International Airport", "United States", 80.0f, 40.6413, -73.7781, "DL"),
            new AirportSeed("SIN", "Singapore Changi Airport", "Singapore", 70.0f, 1.3644, 103.9915, "SQ"),
            new AirportSeed("MAD", "Adolfo Suarez Madrid-Barajas Airport", "Spain", 42.0f, 40.4983, -3.5676, null),
            new AirportSeed("AMS", "Amsterdam Schiphol Airport", "Netherlands", 47.0f, 52.3105, 4.7683, null),
            new AirportSeed("ORY", "Paris-Orly Airport", "France", 55.0f, 48.7262, 2.3652, null),
            new AirportSeed("IST", "Istanbul Airport", "Turkey", 40.0f, 41.2753, 28.7519, null),
            new AirportSeed("MUC", "Munich Airport", "Germany", 50.0f, 48.3538, 11.7861, null),
            new AirportSeed("ATH", "Athens International Airport", "Greece", 45.0f, 37.9364, 23.9475, null),
            new AirportSeed("DUB", "Dublin Airport", "Ireland", 40.0f, 53.4213, -6.2701, null),
            new AirportSeed("HEL", "Helsinki Airport", "Finland", 45.0f, 60.3172, 24.9633, null),
            new AirportSeed("CIA", "Cairo International Airport", "Egypt", 40.0f, 30.1120, 31.4000, null),
            new AirportSeed("KRT", "Kuwait International Airport", "Kuwait", 40.0f, 29.2266, 47.9689, null),
            new AirportSeed("TUN", "Tunis Carthage Airport", "Tunisia", 40.0f, 36.8510, 10.2272, null),
            new AirportSeed("MEL", "Melbourne Airport", "Australia", 40.0f, -37.6690, 144.8410, null),
            new AirportSeed("SYD", "Sydney Airport", "Australia", 40.0f, -33.9399, 151.1753, null),
            new AirportSeed("BOM", "Chhatrapati Shivaji Maharaj International Airport", "India", 40.0f, 19.0896, 72.8656, null),
            new AirportSeed("DEL", "Indira Gandhi International Airport", "India", 40.0f, 28.5562, 77.1000, null)



    );

    private static final List<FlightSeed> FLIGHT_SEEDS = List.of(
            new FlightSeed("CMN", "CDG", "08:30:00", 1230, 180, 12, 12, 0, FlightStatus.SCHEDULED),
            new FlightSeed("CDG", "DXB", "14:05:00", 3240, 240, 15, 5, 0, FlightStatus.SCHEDULED),
            new FlightSeed("CMN", "IST", "06:20:00", 2810, 200, 12, 8, 0, FlightStatus.BOARDING),
            new FlightSeed("IST", "DXB", "10:45:00", 1860, 220, 14, 22, 35, FlightStatus.DELAYED),
            new FlightSeed("DXB", "DOH", "16:30:00", 235, 170, 8, 3, 0, FlightStatus.SCHEDULED),
            new FlightSeed("CDG", "FRA", "09:00:00", 280, 160, 8, 14, 10, FlightStatus.DELAYED),
            new FlightSeed("FRA", "LHR", "12:00:00", 406, 150, 8, 7, 0, FlightStatus.SCHEDULED),
            new FlightSeed("RAK", "CMN", "07:45:00", 125, 150, 6, 1, 0, FlightStatus.SCHEDULED),
            new FlightSeed("LHR", "IST", "18:00:00", 1550, 190, 12, 18, 0, FlightStatus.DEPARTED),
            new FlightSeed("DOH", "LHR", "20:30:00", 3250, 260, 20, 9, 0, FlightStatus.SCHEDULED),
            new FlightSeed("CMN", "DXB", "11:15:00", 3810, 210, 12, 11, 0, FlightStatus.SCHEDULED),
            new FlightSeed("IST", "FRA", "22:00:00", 1160, 165, 8, 6, 0, FlightStatus.LANDED),
            new FlightSeed("LHR", "CDG", "05:30:00", 214, 175, 10, 15, 0, FlightStatus.SCHEDULED),
            new FlightSeed("RAK", "CDG", "15:00:00", 1310, 150, 6, 2, 25, FlightStatus.DELAYED),
            new FlightSeed("FRA", "DOH", "19:30:00", 2820, 220, 14, 20, 0, FlightStatus.SCHEDULED),
            new FlightSeed("JFK", "LHR", "09:45:00", 3450, 280, 20, 30, 0, FlightStatus.SCHEDULED),
            new FlightSeed("LHR", "AMS", "13:25:00", 231, 160, 6, 17, 0, FlightStatus.SCHEDULED),
            new FlightSeed("AMS", "SIN", "21:10:00", 6520, 300, 24, 41, 0, FlightStatus.SCHEDULED),
            new FlightSeed("SIN", "DXB", "04:50:00", 3620, 260, 16, 9, 0, FlightStatus.BOARDING),
            new FlightSeed("MAD", "CMN", "17:20:00", 525, 150, 6, 4, 0, FlightStatus.CANCELLED),
            new FlightSeed("DOH", "SIN", "23:15:00", 3880, 280, 18, 27, 0, FlightStatus.SCHEDULED),
            new FlightSeed("JFK", "CDG", "18:40:00", 3625, 260, 18, 33, 45, FlightStatus.DELAYED),
            new FlightSeed("AMS", "FRA", "07:10:00", 225, 140, 5, 13, 0, FlightStatus.LANDED),
            new FlightSeed("SIN", "JFK", "11:55:00", 9530, 320, 28, 52, 0, FlightStatus.SCHEDULED)

    );

    private static final List<String> PASSENGER_NAMES = List.of(
            "Ahmed Benali",
            "Sofia Martinez",
            "Yuki Tanaka",
            "Omar El Fassi",
            "Emma Thompson",
            "Karim Zidane",
            "Laura Schmidt",
            "Ali Hassan",
            "Marie Dupont",
            "James Wilson",
            "Nora Salem",
            "Lucas Moreau",
            "Hanae Idrissi",
            "David Peterson",
            "Sara Lindholm",
            "Mounir Akhavan",
            "Chloe Bernard",
            "Rami Haddad",
            "Fatima Zahra",
            "Ethan Brooks",
            "Leila Mansouri",
            "Noah Fischer",
            "Aya Nakamura",
            "Hugo Alvarez",
            "Salma El Idrissi",
            "Daniel Cooper",
            "Mila Novak",
            "Yassine El Alaoui",
            "Ines Pereira",
            "Oliver Grant",
            "Rachid El Amrani",
            "Eva Kowalski",
            "Mohamed El Fassi",
            "Lena Nilsson",
            "Maxime Dubois",
            "Camille Leclercq",
            "Rachid El Amrani",
            "Eva Kowalski",
            "Mohamed El Fassi",
            "Lena Nilsson",
            "Maxime Dubois",
            "Camille Leclercq",
            "Rachid El Amrani",
            "Eva Kowalski",
            "Mohamed El Fassi",
            "Lena Nilsson",
            "Maxime Dubois",
            "Camille Leclercq",
            "Jackson Lee",
            "Alicia Wong",
            "David Kim",
            "Sophia Park",
            "Ethan Nguyen",
            "Olivia Tran",
            "Noah Lee",
            "Ava Kim",
            "Mason Nguyen",
            "Isabella Park"
    );
}