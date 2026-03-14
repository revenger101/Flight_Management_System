CREATE TABLE IF NOT EXISTS fare_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    departure_airport_id BIGINT,
    arrival_airport_id BIGINT,
    booking_type VARCHAR(20),
    base_fare DOUBLE,
    base_fare_multiplier DOUBLE,
    refundable BIT NOT NULL,
    change_fee DOUBLE NOT NULL,
    included_baggage_kg INT NOT NULL,
    extra_baggage_fee_per_kg DOUBLE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_fare_rule_departure FOREIGN KEY (departure_airport_id) REFERENCES airport(id),
    CONSTRAINT fk_fare_rule_arrival FOREIGN KEY (arrival_airport_id) REFERENCES airport(id)
);

CREATE TABLE IF NOT EXISTS pricing_campaign (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DOUBLE NOT NULL,
    active BIT NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    departure_airport_id BIGINT,
    arrival_airport_id BIGINT,
    booking_type VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT fk_pricing_campaign_departure FOREIGN KEY (departure_airport_id) REFERENCES airport(id),
    CONSTRAINT fk_pricing_campaign_arrival FOREIGN KEY (arrival_airport_id) REFERENCES airport(id)
);

CREATE TABLE IF NOT EXISTS promo_code (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DOUBLE NOT NULL,
    min_subtotal DOUBLE NOT NULL,
    max_uses INT NOT NULL,
    used_count INT NOT NULL,
    active BIT NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_promo_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS corporate_rate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    corporate_code VARCHAR(40) NOT NULL,
    company_name VARCHAR(120) NOT NULL,
    discount_percent DOUBLE NOT NULL,
    active BIT NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    departure_airport_id BIGINT,
    arrival_airport_id BIGINT,
    booking_type VARCHAR(20),
    PRIMARY KEY (id),
    CONSTRAINT uq_corporate_code UNIQUE (corporate_code),
    CONSTRAINT fk_corporate_rate_departure FOREIGN KEY (departure_airport_id) REFERENCES airport(id),
    CONSTRAINT fk_corporate_rate_arrival FOREIGN KEY (arrival_airport_id) REFERENCES airport(id)
);

ALTER TABLE booking
    ADD COLUMN base_fare DOUBLE,
    ADD COLUMN final_fare DOUBLE,
    ADD COLUMN currency VARCHAR(3),
    ADD COLUMN refundable BIT DEFAULT FALSE,
    ADD COLUMN change_fee DOUBLE,
    ADD COLUMN included_baggage_kg INT,
    ADD COLUMN baggage_kg INT,
    ADD COLUMN extra_baggage_fee DOUBLE,
    ADD COLUMN promo_code VARCHAR(40),
    ADD COLUMN corporate_code VARCHAR(40),
    ADD COLUMN campaign_name VARCHAR(80);
