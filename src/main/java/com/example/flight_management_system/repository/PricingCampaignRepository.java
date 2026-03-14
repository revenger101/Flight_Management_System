package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.PricingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PricingCampaignRepository extends JpaRepository<PricingCampaign, Long> {

    @Query("""
            select c from PricingCampaign c
            where c.active = true
              and c.startsAt <= :now
              and c.endsAt >= :now
            """)
    List<PricingCampaign> findActiveCampaigns(@Param("now") LocalDateTime now);
}
