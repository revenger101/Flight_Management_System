package com.example.flight_management_system.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private UserProfileDTO user;
}
