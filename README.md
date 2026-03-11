# Flight Management System — Spring Boot Backend

## Overview

A RESTful API backend for an airport and flight management system built with **Spring Boot 4.0.3** and **Java 21**. It provides full CRUD operations for airlines, airports, flights, passengers, and bookings, with an H2 in-memory database.

---

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Language runtime |
| Spring Boot | 4.0.3 | Application framework |
| Spring Data JPA | — | ORM & repository abstraction |
| H2 Database | — | In-memory relational database |
| MySQL Connector | — | MySQL driver (runtime, optional) |
| Lombok | — | Boilerplate reduction (getters, setters, constructors) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run the Application

```bash
./mvnw spring-boot:run
```

The server starts on **http://localhost:8080**.

### H2 Console

Access the H2 database console at **http://localhost:8080/h2-console** with:

| Property | Value |
|----------|-------|
| JDBC URL | `jdbc:h2:mem:airplaine` |
| Username | `sa` |
| Password | *(empty)* |

---

## Database Configuration

```properties
spring.application.name=Flight_Management_System
spring.datasource.url=jdbc:h2:mem:airplaine
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## CORS Configuration

Configured in `CorsConfig.java`:

- **Allowed Origins:** `http://localhost:5173` (Vite dev server)
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers:** All (`*`)
- **Path Pattern:** `/api/**`

---

## Project Structure

```
src/main/java/com/example/flight_management_system/
├── FlightManagementSystemApplication.java   # Entry point
├── config/
│   └── CorsConfig.java                     # CORS settings
├── Controller/
│   ├── AirlineController.java
│   ├── AirportController.java
│   ├── BookingController.java
│   ├── FlightController.java
│   └── PassengerController.java
├── dto/
│   ├── AirlineDTO.java
│   ├── AirportDTO.java
│   ├── BookingDTO.java
│   ├── FlightDTO.java
│   ├── FlightHandlingDTO.java
│   ├── MilesAccountDTO.java
│   └── PassengerDTO.java
├── entity/
│   ├── Airline.java
│   ├── Airport.java
│   ├── Booking.java
│   ├── Flight.java
│   ├── FlightHandling.java
│   ├── MilesAccount.java
│   ├── Passenger.java
│   └── enums/
│       └── BookingType.java
├── repository/
│   ├── AirlineRepository.java
│   ├── AirportRepository.java
│   ├── BookingRepository.java
│   ├── FlightHandlingRepository.java
│   └── FlightRepository.java
└── service/
    ├── AirlineService.java
    ├── AirportService.java
    ├── BookingService.java
    ├── FlightService.java
    └── PassengerService.java
```

---

## API Endpoints

Base URL: `http://localhost:8080/api`

### Airlines — `/api/airlines`

| Method | Endpoint | Request Body | Response | Status |
|--------|----------|-------------|----------|--------|
| GET | `/api/airlines` | — | `List<AirlineDTO>` | 200 |
| GET | `/api/airlines/{id}` | — | `AirlineDTO` | 200 |
| POST | `/api/airlines` | `AirlineDTO` | `AirlineDTO` | 201 |
| PUT | `/api/airlines/{id}` | `AirlineDTO` | `AirlineDTO` | 200 |
| DELETE | `/api/airlines/{id}` | — | — | 204 |

**AirlineDTO:**
```json
{
  "id": 1,
  "name": "Royal Air Maroc",
  "shortName": "RAM",
  "logo": "https://example.com/logo.png"
}
```

---

### Airports — `/api/airports`

| Method | Endpoint | Request Body | Response | Status |
|--------|----------|-------------|----------|--------|
| GET | `/api/airports` | — | `List<AirportDTO>` | 200 |
| GET | `/api/airports/{id}` | — | `AirportDTO` | 200 |
| POST | `/api/airports` | `AirportDTO` | `AirportDTO` | 201 |
| PUT | `/api/airports/{id}` | `AirportDTO` | `AirportDTO` | 200 |
| DELETE | `/api/airports/{id}` | — | — | 204 |

**AirportDTO:**
```json
{
  "id": 1,
  "shortName": "CDG",
  "name": "Charles de Gaulle",
  "country": "France",
  "fee": 45.5,
  "airlineId": 1
}
```

---

### Flights — `/api/flights`

| Method | Endpoint | Request Body | Response | Status |
|--------|----------|-------------|----------|--------|
| GET | `/api/flights` | — | `List<FlightDTO>` | 200 |
| GET | `/api/flights/{id}` | — | `FlightDTO` | 200 |
| POST | `/api/flights` | `FlightDTO` | `FlightDTO` | 201 |
| PUT | `/api/flights/{id}` | `FlightDTO` | `FlightDTO` | 200 |
| POST | `/api/flights/{flightId}/connecting/{connectedFlightId}` | — | `FlightDTO` | 200 |
| DELETE | `/api/flights/{id}` | — | — | 204 |

**FlightDTO:**
```json
{
  "id": 1,
  "time": "14:30:00",
  "miles": 1200,
  "departureAirportId": 1,
  "arrivalAirportId": 2,
  "flightHandlings": [
    {
      "id": 1,
      "boardingGate": 12,
      "delay": 15,
      "date": "2026-03-11",
      "time": "14:45:00"
    }
  ],
  "connectingFlightIds": [3, 5]
}
```

---

### Passengers — `/api/passengers`

| Method | Endpoint | Request Body | Response | Status |
|--------|----------|-------------|----------|--------|
| GET | `/api/passengers` | — | `List<PassengerDTO>` | 200 |
| GET | `/api/passengers/{id}` | — | `PassengerDTO` | 200 |
| POST | `/api/passengers` | `PassengerDTO` | `PassengerDTO` | 201 |
| PUT | `/api/passengers/{id}` | `PassengerDTO` | `PassengerDTO` | 200 |
| DELETE | `/api/passengers/{id}` | — | — | 204 |

**PassengerDTO:**
```json
{
  "id": 1,
  "name": "John Doe",
  "cc": "AB123456",
  "mileCard": "MC-0001",
  "status": "Gold",
  "milesAccount": {
    "id": 1,
    "number": "MA-0001",
    "flightMiles": 15000,
    "statusMiles": 8000
  }
}
```

---

### Bookings — `/api/bookings`

| Method | Endpoint | Request Body | Response | Status |
|--------|----------|-------------|----------|--------|
| GET | `/api/bookings` | — | `List<BookingDTO>` | 200 |
| GET | `/api/bookings/{id}` | — | `BookingDTO` | 200 |
| GET | `/api/bookings/passenger/{passengerId}` | — | `List<BookingDTO>` | 200 |
| GET | `/api/bookings/flight/{flightId}` | — | `List<BookingDTO>` | 200 |
| POST | `/api/bookings` | `BookingDTO` | `BookingDTO` | 201 |
| PUT | `/api/bookings/{id}` | `BookingDTO` | `BookingDTO` | 200 |
| DELETE | `/api/bookings/{id}` | — | — | 204 |

**BookingDTO:**
```json
{
  "id": 1,
  "kind": "One-way",
  "date": "2026-03-15",
  "type": "BUSINESS",
  "passengerId": 1,
  "flightId": 2
}
```

---

## Entity Relationships

```
Airline ──── 1:1 ──── Airport
                        │
                       1:N
                        │
                      Flight ──── N:N ──── Flight (connecting flights)
                        │
                       1:N
                        │
                   ┌────┴────┐
                   │         │
              Booking    FlightHandling
                   │
                  N:1
                   │
              Passenger ──── 1:1 ──── MilesAccount
```

### Relationship Details

| Relationship | Type | Cascade | Join Column |
|-------------|------|---------|-------------|
| Airline ↔ Airport | One-to-One | None | `airline_id` on Airport |
| Airport → Flight (departure) | One-to-Many | None | `departure_airport_id` on Flight |
| Airport → Flight (arrival) | One-to-Many | None | `arrival_airport_id` on Flight |
| Flight → FlightHandling | One-to-Many | ALL | `flight_id` on FlightHandling |
| Flight ↔ Flight (connecting) | Many-to-Many | None | Join table `connecting_flights` |
| Flight → Booking | One-to-Many | None | `flight_id` on Booking |
| Passenger → Booking | One-to-Many | ALL | `passenger_id` on Booking |
| Passenger → MilesAccount | One-to-One | ALL | `miles_account_id` on Passenger |

---

## Entities

### Airline
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| name | String | — |
| shortName | String | — |
| logo | String | — |

### Airport
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| shortName | String | — |
| name | String | — |
| country | String | — |
| fee | float | — |
| airline | Airline | @OneToOne, @JoinColumn(airline_id) |
| departingFlights | List\<Flight\> | @OneToMany(mappedBy="departureAirport") |
| arrivingFlights | List\<Flight\> | @OneToMany(mappedBy="arrivalAirport") |

### Flight
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| time | LocalTime | — |
| miles | int | — |
| departureAirport | Airport | @ManyToOne, @JoinColumn(departure_airport_id) |
| arrivalAirport | Airport | @ManyToOne, @JoinColumn(arrival_airport_id) |
| flightHandlings | List\<FlightHandling\> | @OneToMany(cascade=ALL), @JoinColumn(flight_id) |
| connectingFlights | List\<Flight\> | @ManyToMany, @JoinTable(connecting_flights) |
| connectedByFlights | List\<Flight\> | @ManyToMany(mappedBy="connectingFlights") |
| bookings | List\<Booking\> | @OneToMany(mappedBy="flight") |

### FlightHandling
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| boardingGate | int | — |
| delay | int | — |
| date | LocalDate | — |
| time | LocalTime | — |

### Passenger
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| name | String | — |
| cc | String | — |
| mileCard | String | — |
| status | String | — |
| milesAccount | MilesAccount | @OneToOne(cascade=ALL), @JoinColumn(miles_account_id) |
| bookings | List\<Booking\> | @OneToMany(mappedBy="passenger", cascade=ALL) |

### MilesAccount
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| number | String | — |
| flightMiles | int | — |
| statusMiles | int | — |
| passenger | Passenger | @OneToOne(mappedBy="milesAccount") |

### Booking
| Field | Type | Annotation |
|-------|------|------------|
| id | Long | @Id, @GeneratedValue(IDENTITY) |
| kind | String | — |
| date | LocalDate | — |
| type | BookingType | @Enumerated(STRING) |
| passenger | Passenger | @ManyToOne, @JoinColumn(passenger_id) |
| flight | Flight | @ManyToOne, @JoinColumn(flight_id) |

### Enums

**BookingType:**
```java
public enum BookingType {
    ECONOMIC,
    BUSINESS
}
```

---

## Service Layer

Each service handles DTO ↔ Entity conversion and business logic.

### AirlineService
| Method | Description |
|--------|-------------|
| `findAll()` | Returns all airlines as DTOs |
| `findById(Long id)` | Returns airline by ID (throws RuntimeException if not found) |
| `create(AirlineDTO)` | Creates a new airline |
| `update(Long id, AirlineDTO)` | Updates name, shortName, logo |
| `delete(Long id)` | Deletes airline by ID |

### AirportService
| Method | Description |
|--------|-------------|
| `findAll()` | Returns all airports as DTOs |
| `findById(Long id)` | Returns airport by ID |
| `create(AirportDTO)` | Creates airport with optional airline association |
| `update(Long id, AirportDTO)` | Updates airport and optionally reassigns airline |
| `delete(Long id)` | Deletes airport by ID |

### FlightService
| Method | Description |
|--------|-------------|
| `findAll()` | Returns all flights with handlings and connecting flights |
| `findById(Long id)` | Returns flight by ID |
| `create(FlightDTO)` | Creates flight with departure/arrival airports and handlings |
| `update(Long id, FlightDTO)` | Updates time, miles, and airports |
| `addConnectingFlight(Long, Long)` | Links two flights as connecting |
| `delete(Long id)` | Deletes flight by ID |

### PassengerService
| Method | Description |
|--------|-------------|
| `findAll()` | Returns all passengers with miles accounts |
| `findById(Long id)` | Returns passenger by ID |
| `create(PassengerDTO)` | Creates passenger with optional miles account (cascaded) |
| `update(Long id, PassengerDTO)` | Updates name, cc, mileCard, status |
| `delete(Long id)` | Deletes passenger by ID |

### BookingService
| Method | Description |
|--------|-------------|
| `findAll()` | Returns all bookings as DTOs |
| `findById(Long id)` | Returns booking by ID |
| `findByPassengerId(Long)` | Filters bookings by passenger ID |
| `findByFlightId(Long)` | Filters bookings by flight ID |
| `create(BookingDTO)` | Creates booking (validates passenger & flight exist) |
| `update(Long id, BookingDTO)` | Updates kind, date, type |
| `delete(Long id)` | Deletes booking by ID |

---

## Repositories

All repositories extend `JpaRepository` and inherit standard CRUD operations.

| Repository | Entity | Custom Methods |
|-----------|--------|----------------|
| AirlineRepository | Airline | — |
| AirportRepository | Airport | — |
| FlightRepository | Flight | — |
| FlightHandlingRepository | FlightHandling | — |
| BookingRepository | Booking | `findByFlightMilesLessThan(int)`, `findBytype(BookingType)` |
