CREATE TABLE IF NOT EXISTS airline (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120),
    short_name VARCHAR(20),
    logo VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS airport (
    id BIGINT NOT NULL AUTO_INCREMENT,
    short_name VARCHAR(20),
    name VARCHAR(120),
    country VARCHAR(80),
    fee FLOAT,
    airline_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_airport_airline FOREIGN KEY (airline_id) REFERENCES airline(id)
);

CREATE TABLE IF NOT EXISTS miles_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    number VARCHAR(60),
    flight_miles INT,
    status_miles INT,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS passenger (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120),
    cc VARCHAR(30),
    mile_card VARCHAR(40),
    status VARCHAR(30),
    miles_account_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uq_passenger_miles_account UNIQUE (miles_account_id),
    CONSTRAINT fk_passenger_miles_account FOREIGN KEY (miles_account_id) REFERENCES miles_account(id)
);

CREATE TABLE IF NOT EXISTS flight (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT,
    time TIME(6),
    miles INT,
    seat_capacity INT,
    overbooking_limit INT,
    waitlist_enabled BIT,
    current_gate INT,
    delay_minutes INT,
    status VARCHAR(30),
    departure_airport_id BIGINT,
    arrival_airport_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_flight_departure_airport FOREIGN KEY (departure_airport_id) REFERENCES airport(id),
    CONSTRAINT fk_flight_arrival_airport FOREIGN KEY (arrival_airport_id) REFERENCES airport(id)
);

CREATE TABLE IF NOT EXISTS flight_handling (
    id BIGINT NOT NULL AUTO_INCREMENT,
    boarding_gate INT,
    delay INT,
    date DATE,
    time TIME(6),
    flight_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_flight_handling_flight FOREIGN KEY (flight_id) REFERENCES flight(id)
);

CREATE TABLE IF NOT EXISTS connecting_flights (
    flight_id BIGINT NOT NULL,
    connected_flight_id BIGINT NOT NULL,
    PRIMARY KEY (flight_id, connected_flight_id),
    CONSTRAINT fk_connecting_flights_flight FOREIGN KEY (flight_id) REFERENCES flight(id),
    CONSTRAINT fk_connecting_flights_connected FOREIGN KEY (connected_flight_id) REFERENCES flight(id)
);

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT NOT NULL AUTO_INCREMENT,
    version BIGINT,
    kind VARCHAR(40),
    date DATE,
    type VARCHAR(30),
    status VARCHAR(30),
    idempotency_key VARCHAR(80),
    cancellation_reason VARCHAR(255),
    rebooked_to_flight_id BIGINT,
    passenger_id BIGINT,
    flight_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uq_booking_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_booking_passenger FOREIGN KEY (passenger_id) REFERENCES passenger(id),
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES flight(id)
);

CREATE TABLE IF NOT EXISTS loyalty_ledger (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(30),
    miles INT,
    note VARCHAR(255),
    created_at DATETIME(6),
    passenger_id BIGINT,
    booking_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_loyalty_ledger_passenger FOREIGN KEY (passenger_id) REFERENCES passenger(id),
    CONSTRAINT fk_loyalty_ledger_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

CREATE TABLE IF NOT EXISTS notification_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(40),
    channel VARCHAR(30),
    message VARCHAR(255),
    created_at DATETIME(6),
    flight_id BIGINT,
    booking_id BIGINT,
    passenger_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_event_flight FOREIGN KEY (flight_id) REFERENCES flight(id),
    CONSTRAINT fk_notification_event_booking FOREIGN KEY (booking_id) REFERENCES booking(id),
    CONSTRAINT fk_notification_event_passenger FOREIGN KEY (passenger_id) REFERENCES passenger(id)
);
