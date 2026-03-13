package com.example.flight_management_system.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthSessionResult {
    private AuthResponseDTO authResponse;
    private String refreshToken;
}
