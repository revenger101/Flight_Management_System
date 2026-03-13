-- ============================================================
-- Flight Management System - Seed Data
-- Database: H2 In-Memory (jdbc:h2:mem:airplaine)
-- ============================================================

-- =====================
-- 1. AIRLINES
-- =====================
INSERT INTO airline (id, name, short_name, logo) VALUES (1, 'Royal Air Maroc', 'RAM', 'https://play-lh.googleusercontent.com/VpqfdMhLvOYnDOy2-wAe29eX3HsPNh5Hc4X7z2KUq1FxKhx4xREWAXDc0HsNapfJly8=w240-h480-rw');
INSERT INTO airline (id, name, short_name, logo) VALUES (2, 'Air France', 'AF', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Air_France_Logo.svg/200px-Air_France_Logo.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (3, 'Emirates', 'EK', 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Emirates_logo.svg/200px-Emirates_logo.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (4, 'Turkish Airlines', 'TK', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/00/Turkish_Airlines_logo_2019_compact.svg/200px-Turkish_Airlines_logo_2019_compact.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (5, 'Qatar Airways', 'QR', 'https://upload.wikimedia.org/wikipedia/en/thumb/9/9b/Qatar_Airways_Logo.svg/200px-Qatar_Airways_Logo.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (6, 'Lufthansa', 'LH', 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Lufthansa_Logo_2018.svg/200px-Lufthansa_Logo_2018.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (7, 'British Airways', 'BA', 'https://upload.wikimedia.org/wikipedia/en/thumb/4/42/British_Airways_Logo.svg/200px-British_Airways_Logo.svg.png');
INSERT INTO airline (id, name, short_name, logo) VALUES (8, 'Ryanair', 'FR', 'https://play-lh.googleusercontent.com/UlvFF-Zo2h6_8RdoMh9xWbAcaqSrsIU_yhQPOcH5rbTQ7Av9EvfWFTrAen1EX4X-JxA_');

-- =====================
-- 2. AIRPORTS
-- =====================
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (1, 'CMN', 'Mohammed V International Airport', 'Morocco', 45.00, 1);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (2, 'CDG', 'Charles de Gaulle Airport', 'France', 65.00, 2);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (3, 'DXB', 'Dubai International Airport', 'UAE', 55.00, 3);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (4, 'IST', 'Istanbul Airport', 'Turkey', 40.00, 4);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (5, 'DOH', 'Hamad International Airport', 'Qatar', 50.00, 5);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (6, 'FRA', 'Frankfurt Airport', 'Germany', 60.00, 6);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (7, 'LHR', 'London Heathrow Airport', 'United Kingdom', 75.00, 7);
INSERT INTO airport (id, short_name, name, country, fee, airline_id) VALUES (8, 'RAK', 'Marrakech Menara Airport', 'Morocco', 35.00, 8);

-- =====================
-- 3. FLIGHTS
-- =====================
-- Direct flights
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (1, '08:30:00', 1200, 1, 2);   -- CMN -> CDG
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (2, '14:00:00', 3100, 2, 3);   -- CDG -> DXB
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (3, '06:15:00', 2800, 1, 4);   -- CMN -> IST
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (4, '10:45:00', 4200, 4, 3);   -- IST -> DXB
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (5, '16:30:00', 1500, 3, 5);   -- DXB -> DOH
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (6, '09:00:00', 600, 2, 6);    -- CDG -> FRA
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (7, '12:00:00', 220, 6, 7);    -- FRA -> LHR
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (8, '07:45:00', 300, 8, 1);    -- RAK -> CMN
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (9, '18:00:00', 1800, 7, 4);   -- LHR -> IST
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (10, '20:30:00', 3500, 5, 7);  -- DOH -> LHR
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (11, '11:15:00', 2500, 1, 3);  -- CMN -> DXB
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (12, '22:00:00', 950, 4, 6);   -- IST -> FRA
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (13, '05:30:00', 1100, 7, 2);  -- LHR -> CDG
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (14, '15:00:00', 400, 8, 2);   -- RAK -> CDG
INSERT INTO flight (id, time, miles, departure_airport_id, arrival_airport_id) VALUES (15, '19:30:00', 2700, 6, 5);  -- FRA -> DOH

-- =====================
-- 4. CONNECTING FLIGHTS (Many-to-Many self-reference)
-- =====================
-- CMN->CDG connects to CDG->DXB (flight 1 -> flight 2)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (1, 2);
-- CMN->CDG connects to CDG->FRA (flight 1 -> flight 6)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (1, 6);
-- CDG->FRA connects to FRA->LHR (flight 6 -> flight 7)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (6, 7);
-- CMN->IST connects to IST->DXB (flight 3 -> flight 4)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (3, 4);
-- CMN->IST connects to IST->FRA (flight 3 -> flight 12)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (3, 12);
-- DXB->DOH connects to DOH->LHR (flight 5 -> flight 10)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (5, 10);
-- RAK->CMN connects to CMN->CDG (flight 8 -> flight 1)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (8, 1);
-- RAK->CMN connects to CMN->DXB (flight 8 -> flight 11)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (8, 11);
-- LHR->IST connects to IST->DXB (flight 9 -> flight 4)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (9, 4);
-- FRA->DOH connects to DOH->LHR (flight 15 -> flight 10)
INSERT INTO connecting_flights (flight_id, connected_flight_id) VALUES (15, 10);

-- =====================
-- 5. FLIGHT HANDLING
-- =====================
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (1, 12, 0, '2026-03-15', '08:00:00', 1);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (2, 5, 15, '2026-03-15', '13:45:00', 2);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (3, 8, 0, '2026-03-15', '05:45:00', 3);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (4, 22, 30, '2026-03-15', '10:15:00', 4);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (5, 3, 0, '2026-03-16', '16:00:00', 5);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (6, 14, 10, '2026-03-16', '08:30:00', 6);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (7, 7, 0, '2026-03-16', '11:30:00', 7);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (8, 1, 5, '2026-03-16', '07:15:00', 8);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (9, 18, 45, '2026-03-17', '17:30:00', 9);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (10, 9, 0, '2026-03-17', '20:00:00', 10);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (11, 11, 20, '2026-03-17', '10:45:00', 11);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (12, 6, 0, '2026-03-18', '21:30:00', 12);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (13, 15, 0, '2026-03-18', '05:00:00', 13);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (14, 2, 25, '2026-03-18', '14:30:00', 14);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (15, 20, 0, '2026-03-19', '19:00:00', 15);
-- Additional handling entries for same flights on different dates
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (16, 10, 0, '2026-03-20', '08:00:00', 1);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (17, 4, 60, '2026-03-20', '13:30:00', 2);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (18, 16, 0, '2026-03-20', '06:00:00', 3);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (19, 19, 0, '2026-03-21', '16:15:00', 5);
INSERT INTO flight_handling (id, boarding_gate, delay, date, time, flight_id) VALUES (20, 13, 10, '2026-03-21', '08:45:00', 6);

-- =====================
-- 6. MILES ACCOUNTS
-- =====================
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (1, 'MA-2026-001', 52000, 12000);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (2, 'MA-2026-002', 15000, 3500);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (3, 'MA-2026-003', 120000, 45000);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (4, 'MA-2026-004', 8000, 1200);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (5, 'MA-2026-005', 200000, 80000);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (6, 'MA-2026-006', 34000, 9500);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (7, 'MA-2026-007', 67000, 22000);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (8, 'MA-2026-008', 3000, 500);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (9, 'MA-2026-009', 91000, 35000);
INSERT INTO miles_account (id, number, flight_miles, status_miles) VALUES (10, 'MA-2026-010', 145000, 60000);

-- =====================
-- 7. PASSENGERS
-- =====================
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (1, 'Ahmed Benali', 'AB123456', 'GOLD', 'Gold Member', 1);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (2, 'Sofia Martinez', 'SM789012', 'SILVER', 'Silver Member', 2);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (3, 'Yuki Tanaka', 'YT345678', 'PLATINUM', 'Platinum Member', 3);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (4, 'Omar El Fassi', 'OE901234', 'BASIC', 'Basic Member', 4);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (5, 'Emma Thompson', 'ET567890', 'PLATINUM', 'Platinum Member', 5);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (6, 'Karim Zidane', 'KZ112233', 'GOLD', 'Gold Member', 6);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (7, 'Laura Schmidt', 'LS445566', 'GOLD', 'Gold Member', 7);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (8, 'Ali Hassan', 'AH778899', 'BASIC', 'Basic Member', 8);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (9, 'Marie Dupont', 'MD334455', 'PLATINUM', 'Platinum Member', 9);
INSERT INTO passenger (id, name, cc, mile_card, status, miles_account_id) VALUES (10, 'James Wilson', 'JW667788', 'PLATINUM', 'Platinum Member', 10);

-- =====================
-- 8. BOOKINGS
-- =====================
-- Ahmed Benali bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (1, 'One-way', '2026-03-15', 'BUSINESS', 1, 1);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (2, 'Connecting', '2026-03-15', 'BUSINESS', 1, 2);

-- Sofia Martinez bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (3, 'One-way', '2026-03-15', 'ECONOMIC', 2, 3);

-- Yuki Tanaka bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (4, 'Round-trip', '2026-03-16', 'BUSINESS', 3, 5);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (5, 'Round-trip', '2026-03-17', 'BUSINESS', 3, 10);

-- Omar El Fassi bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (6, 'One-way', '2026-03-16', 'ECONOMIC', 4, 6);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (7, 'Connecting', '2026-03-16', 'ECONOMIC', 4, 7);

-- Emma Thompson bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (8, 'One-way', '2026-03-17', 'BUSINESS', 5, 9);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (9, 'Connecting', '2026-03-17', 'BUSINESS', 5, 4);

-- Karim Zidane bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (10, 'One-way', '2026-03-17', 'ECONOMIC', 6, 11);

-- Laura Schmidt bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (11, 'Round-trip', '2026-03-18', 'BUSINESS', 7, 13);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (12, 'Round-trip', '2026-03-19', 'BUSINESS', 7, 1);

-- Ali Hassan bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (13, 'One-way', '2026-03-16', 'ECONOMIC', 8, 8);

-- Marie Dupont bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (14, 'One-way', '2026-03-18', 'BUSINESS', 9, 14);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (15, 'Connecting', '2026-03-18', 'BUSINESS', 9, 2);

-- James Wilson bookings
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (16, 'Round-trip', '2026-03-19', 'BUSINESS', 10, 15);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (17, 'Round-trip', '2026-03-20', 'BUSINESS', 10, 10);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (18, 'One-way', '2026-03-20', 'ECONOMIC', 6, 1);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (19, 'One-way', '2026-03-20', 'ECONOMIC', 2, 14);
INSERT INTO booking (id, kind, date, type, passenger_id, flight_id) VALUES (20, 'One-way', '2026-03-21', 'BUSINESS', 1, 11);
