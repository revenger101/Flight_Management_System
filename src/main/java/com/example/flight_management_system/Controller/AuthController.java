package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.AuthResponseDTO;
import com.example.flight_management_system.dto.LoginRequestDTO;
import com.example.flight_management_system.dto.RegisterRequestDTO;
import com.example.flight_management_system.dto.UserProfileDTO;
import com.example.flight_management_system.service.AuthService;
import com.example.flight_management_system.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        var session = authService.register(request);
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.attachRefreshCookie(headers, session.getRefreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(session.getAuthResponse());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        var session = authService.login(request);
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.attachRefreshCookie(headers, session.getRefreshToken());
        return ResponseEntity.ok().headers(headers).body(session.getAuthResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(HttpServletRequest request) {
        var session = authService.refresh(refreshTokenService.extractRefreshToken(request));
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.attachRefreshCookie(headers, session.getRefreshToken());
        return ResponseEntity.ok().headers(headers).body(session.getAuthResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(refreshTokenService.extractRefreshToken(request));
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.clearRefreshCookie(headers);
        return ResponseEntity.noContent().headers(headers).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> me(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(authService.me(principal.getUsername()));
    }
}
