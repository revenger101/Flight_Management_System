package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.LoyaltyLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyLedgerRepository extends JpaRepository<LoyaltyLedger, Long> {
    List<LoyaltyLedger> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
}
