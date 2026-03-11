package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.MilesAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilesAccountRepository extends JpaRepository<MilesAccount , Long> {
}
