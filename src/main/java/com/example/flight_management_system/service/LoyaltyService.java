package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.LoyaltyLedgerDTO;
import com.example.flight_management_system.entity.Booking;
import com.example.flight_management_system.entity.LoyaltyLedger;
import com.example.flight_management_system.entity.MilesAccount;
import com.example.flight_management_system.entity.Passenger;
import com.example.flight_management_system.entity.enums.LoyaltyLedgerType;
import com.example.flight_management_system.repository.LoyaltyLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyLedgerRepository loyaltyLedgerRepository;

    public void accrueMiles(Passenger passenger, Booking booking, int miles, String note) {
        MilesAccount account = ensureMilesAccount(passenger);
        account.setFlightMiles(account.getFlightMiles() + miles);
        account.setStatusMiles(account.getStatusMiles() + miles);
        passenger.setStatus(tierForMiles(account.getStatusMiles()));
        saveLedger(passenger, booking, LoyaltyLedgerType.ACCRUAL, miles, note);
    }

    public void reverseMiles(Passenger passenger, Booking booking, int miles, String note) {
        MilesAccount account = ensureMilesAccount(passenger);
        account.setFlightMiles(Math.max(0, account.getFlightMiles() - miles));
        account.setStatusMiles(Math.max(0, account.getStatusMiles() - miles));
        passenger.setStatus(tierForMiles(account.getStatusMiles()));
        saveLedger(passenger, booking, LoyaltyLedgerType.REVERSAL, miles, note);
    }

    public List<LoyaltyLedgerDTO> findByPassengerId(Long passengerId) {
        return loyaltyLedgerRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private MilesAccount ensureMilesAccount(Passenger passenger) {
        if (passenger.getMilesAccount() != null) return passenger.getMilesAccount();

        MilesAccount account = MilesAccount.builder()
                .number("AUTO-" + passenger.getId())
                .flightMiles(0)
                .statusMiles(0)
                .passenger(passenger)
                .build();
        passenger.setMilesAccount(account);
        return account;
    }

    private void saveLedger(Passenger passenger, Booking booking, LoyaltyLedgerType type, int miles, String note) {
        loyaltyLedgerRepository.save(LoyaltyLedger.builder()
                .type(type)
                .miles(miles)
                .note(note)
                .createdAt(LocalDateTime.now())
                .passenger(passenger)
                .booking(booking)
                .build());
    }

    private String tierForMiles(int statusMiles) {
        if (statusMiles >= 30000) return "Platinum";
        if (statusMiles >= 15000) return "Gold";
        if (statusMiles >= 5000) return "Silver";
        return "Bronze";
    }

    private LoyaltyLedgerDTO toDTO(LoyaltyLedger ledger) {
        return LoyaltyLedgerDTO.builder()
                .id(ledger.getId())
                .type(ledger.getType())
                .miles(ledger.getMiles())
                .note(ledger.getNote())
                .createdAt(ledger.getCreatedAt())
                .passengerId(ledger.getPassenger() != null ? ledger.getPassenger().getId() : null)
                .bookingId(ledger.getBooking() != null ? ledger.getBooking().getId() : null)
                .build();
    }
}
