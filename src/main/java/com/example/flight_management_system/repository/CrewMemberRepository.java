package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.CrewMember;
import com.example.flight_management_system.entity.enums.CrewRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    List<CrewMember> findByAvailableTrue();
    List<CrewMember> findByRole(CrewRole role);
}
