package com.portal.escolar.controllers;

import com.portal.escolar.dto.AuthDtos.AuthResponse;
import com.portal.escolar.dto.AuthDtos.ChangePasswordRequest;
import com.portal.escolar.dto.AuthDtos.LoginRequest;
import com.portal.escolar.dto.AuthDtos.UserSummary;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.cookie-name}")
    private String cookieName;

    @Value("${app.jwt.cookie-secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        AuthService.LoginResult result = authService.login(request, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie(result.refreshToken(), result.refreshMaxAgeSeconds()).toString());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        String token = readCookie(httpRequest);
        AuthService.LoginResult result = authService.refresh(token, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie(result.refreshToken(), result.refreshMaxAgeSeconds()).toString());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest httpRequest,
                                                      HttpServletResponse httpResponse) {
        String token = readCookie(httpRequest);
        authService.logout(token);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummary> me() {
        return ResponseEntity.ok(authService.me(CurrentUser.require()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                              HttpServletResponse httpResponse) {
        authService.changePassword(CurrentUser.require(), request);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada. Inicia sesión de nuevo."));
    }

    private ResponseCookie cookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    private String readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
