package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.CorporateRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorporateRateRepository extends JpaRepository<CorporateRate, Long> {
    Optional<CorporateRate> findByCorporateCodeIgnoreCase(String corporateCode);
}
