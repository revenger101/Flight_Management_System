package com.example.flight_management_system.service;

import com.example.flight_management_system.entity.AppUser;
import com.example.flight_management_system.entity.RefreshToken;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.refresh.expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.security.refresh.cookie-name}")
    private String refreshCookieName;

    @Value("${app.security.refresh.cookie-secure}")
    private boolean refreshCookieSecure;

    @Value("${app.security.refresh.cookie-same-site}")
    private String refreshCookieSameSite;

    @Transactional
    public String issueRefreshToken(AppUser user) {
        revokeActiveTokensForUser(user.getId());

        byte[] buffer = new byte[48];
        secureRandom.nextBytes(buffer);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpirationMs)))
                .revoked(false)
                .user(user)
                .build());

        return rawToken;
    }

    @Transactional
    public String rotateRefreshToken(String rawToken) {
        RefreshToken current = validateRefreshToken(rawToken);
        current.setRevoked(true);
        current.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(current);
        return issueRefreshToken(current.getUser());
    }

    @Transactional
    public void revokeByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findByTokenHash(hashToken(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    public RefreshToken validateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Refresh token is missing");
        }

        RefreshToken token = refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token is no longer valid");
        }

        return token;
    }

    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (refreshCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void attachRefreshCookie(org.springframework.http.HttpHeaders headers, String rawToken) {
        headers.add(HttpHeaders.SET_COOKIE, buildCookie(rawToken, refreshExpirationMs).toString());
    }

    public void clearRefreshCookie(HttpHeaders headers) {
        headers.add(HttpHeaders.SET_COOKIE, buildCookie("", 0).toString());
    }

    private ResponseCookie buildCookie(String value, long maxAgeMs) {
        return ResponseCookie.from(refreshCookieName, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/")
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    @Transactional
    protected void revokeActiveTokensForUser(Long userId) {
        refreshTokenRepository.findAllByUserIdAndRevokedFalseAndExpiresAtAfter(userId, LocalDateTime.now())
                .forEach(token -> {
                    token.setRevoked(true);
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required", ex);
        }
    }
}
