package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.AuthResponseDTO;
import com.example.flight_management_system.dto.AuthSessionResult;
import com.example.flight_management_system.dto.LoginRequestDTO;
import com.example.flight_management_system.dto.RegisterRequestDTO;
import com.example.flight_management_system.dto.UserProfileDTO;
import com.example.flight_management_system.entity.AppUser;
import com.example.flight_management_system.entity.RefreshToken;
import com.example.flight_management_system.entity.enums.AuthProvider;
import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.AppUserRepository;
import com.example.flight_management_system.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
        public AuthSessionResult register(RegisterRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        if (appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered");
        }

        AppUser user = AppUser.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .role(UserRole.USER)
                .enabled(true)
                .build();

        AppUser saved = appUserRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        return buildSession(saved, jwtService.generateToken(authentication));
    }

    public AuthSessionResult login(LoginRequestDTO request) {
        String email = request.getEmail().trim().toLowerCase();

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProvider() == AuthProvider.GOOGLE && (user.getPasswordHash() == null || user.getPasswordHash().isBlank())) {
            throw new BadRequestException("This account uses Google login. Use OAuth2 with Google.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        return buildSession(user, jwtService.generateToken(authentication));
    }

    public AuthSessionResult refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);
        AppUser user = refreshToken.getUser();

        String rotatedToken = refreshTokenService.rotateRefreshToken(rawRefreshToken);
        return AuthSessionResult.builder()
                .authResponse(AuthResponseDTO.builder()
                        .token(jwtService.generateTokenForUser(user))
                        .tokenType("Bearer")
                        .expiresInSeconds(expirationMs / 1000)
                        .user(toUserProfile(user))
                        .build())
                .refreshToken(rotatedToken)
                .build();
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRawToken(rawRefreshToken);
    }

    public UserProfileDTO me(String email) {
        AppUser user = appUserRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        return toUserProfile(user);
    }

    private UserProfileDTO toUserProfile(AppUser user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .build();
    }

        public AuthSessionResult issueOAuthSession(AppUser user) {
                return buildSession(user, jwtService.generateTokenForUser(user));
        }

        private AuthSessionResult buildSession(AppUser user, String accessToken) {
                return AuthSessionResult.builder()
                                .authResponse(AuthResponseDTO.builder()
                                                .token(accessToken)
                                                .tokenType("Bearer")
                                                .expiresInSeconds(expirationMs / 1000)
                                                .user(toUserProfile(user))
                                                .build())
                                .refreshToken(refreshTokenService.issueRefreshToken(user))
                                .build();
        }
}
