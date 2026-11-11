package com.portal.escolar.services;

import com.portal.escolar.dto.AuthDtos.AuthResponse;
import com.portal.escolar.dto.AuthDtos.ChangePasswordRequest;
import com.portal.escolar.dto.AuthDtos.LoginRequest;
import com.portal.escolar.dto.AuthDtos.UserSummary;
import com.portal.escolar.entities.DomainEnums.Role;
import com.portal.escolar.entities.RefreshToken;
import com.portal.escolar.entities.User;
import com.portal.escolar.exceptions.TooManyRequestsException;
import com.portal.escolar.repositories.ParentProfileRepository;
import com.portal.escolar.repositories.RefreshTokenRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import com.portal.escolar.repositories.UserRepository;
import com.portal.escolar.security.JwtService;
import com.portal.escolar.security.LoginAttemptService;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_HASH = "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.jwt.refresh-token-days}")
    private long refreshTokenDays;

    public record LoginResult(AuthResponse response, String refreshToken, long refreshMaxAgeSeconds) {
    }

    @Transactional
    public LoginResult login(LoginRequest request, String userAgent, String ip) {
        String key = request.email().toLowerCase() + "|" + ip;
        if (loginAttemptService.isBlocked(key)) {
            throw new TooManyRequestsException("Demasiados intentos fallidos. Intenta de nuevo en "
                    + loginAttemptService.retryAfterSeconds(key) / 60 + " minutos.");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
        if (user == null) {
            passwordEncoder.matches(request.password(), DUMMY_HASH);
            loginAttemptService.onFailure(key);
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.onFailure(key);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        loginAttemptService.onSuccess(key);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String rawRefresh = createRefreshToken(user, userAgent, ip);
        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResult(
                new AuthResponse(accessToken, jwtService.getAccessSeconds(), toSummary(user)),
                rawRefresh,
                refreshTokenDays * 86_400L
        );
    }

    @Transactional
    public LoginResult refresh(String rawToken, String userAgent, String ip) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadCredentialsException("Sesión expirada");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Sesión expirada"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new BadCredentialsException("Sesión expirada");
        }
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Sesión expirada"));
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Sesión expirada");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newRefresh = createRefreshToken(user, userAgent, ip);
        String accessToken = jwtService.generateAccessToken(user);
        return new LoginResult(
                new AuthResponse(accessToken, jwtService.getAccessSeconds(), toSummary(user)),
                newRefresh,
                refreshTokenDays * 86_400L
        );
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional(readOnly = true)
    public UserSummary me(UserPrincipal principal) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida"));
        return toSummary(user);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new com.portal.escolar.exceptions.BusinessException("La nueva contraseña debe ser diferente a la actual");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(user.getId());
    }

    public UserSummary toSummary(User user) {
        UUID profileId = switch (user.getRole()) {
            case STUDENT -> studentProfileRepository.findByUserId(user.getId()).map(p -> p.getId()).orElse(null);
            case TEACHER -> teacherProfileRepository.findByUserId(user.getId()).map(p -> p.getId()).orElse(null);
            case PARENT -> parentProfileRepository.findByUserId(user.getId()).map(p -> p.getId()).orElse(null);
            case ADMIN -> null;
        };
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getRole().name(),
                user.isMustChangePassword(),
                profileId
        );
    }

    private String createRefreshToken(User user, String userAgent, String ip) {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = new RefreshToken();
        token.setUserId(user.getId());
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        token.setUserAgent(userAgent == null ? null : userAgent.substring(0, Math.min(userAgent.length(), 300)));
        token.setIp(ip);
        refreshTokenRepository.save(token);
        return raw;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }

    public static boolean isAdmin(UserPrincipal principal) {
        return principal.role() == Role.ADMIN;
    }
}
