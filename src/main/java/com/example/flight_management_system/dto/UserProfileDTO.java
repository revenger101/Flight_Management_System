package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.AuthProvider;
import com.example.flight_management_system.entity.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private AuthProvider provider;
}
