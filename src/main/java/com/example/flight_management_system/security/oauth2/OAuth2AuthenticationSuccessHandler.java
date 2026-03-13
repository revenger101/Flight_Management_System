package com.example.flight_management_system.security.oauth2;

import com.example.flight_management_system.dto.AuthSessionResult;
import com.example.flight_management_system.entity.AppUser;
import com.example.flight_management_system.entity.enums.AuthProvider;
import com.example.flight_management_system.entity.enums.UserRole;
import com.example.flight_management_system.repository.AppUserRepository;
import com.example.flight_management_system.service.AuthService;
import com.example.flight_management_system.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.security.oauth2.success-redirect-uri}")
    private String successRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String fullName = oauth2User.getAttribute("name");
        String providerId = oauth2User.getAttribute("sub");

        if (email == null || email.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account email is required");
            return;
        }

        AppUser user = appUserRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> appUserRepository.save(AppUser.builder()
                        .fullName(fullName != null ? fullName : email)
                        .provider(AuthProvider.GOOGLE)
                        .providerId(providerId)
                        .role(UserRole.USER)
                        .enabled(true)
                        .email(email.toLowerCase())
                        .build()));

        AuthSessionResult session = authService.issueOAuthSession(user);

        String target = UriComponentsBuilder
                .fromUriString(successRedirectUri)
                .queryParam("token", session.getAuthResponse().getToken())
                .queryParam("email", user.getEmail())
                .build()
                .toUriString();

        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookieHeader(session));

        response.sendRedirect(target);
    }

    private String buildRefreshCookieHeader(AuthSessionResult session) {
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.attachRefreshCookie(headers, session.getRefreshToken());
        return headers.getFirst(HttpHeaders.SET_COOKIE);
    }
}
