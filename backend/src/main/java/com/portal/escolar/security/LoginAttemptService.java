package com.portal.escolar.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de intentos de inicio de sesión en memoria. Suficiente para una instancia.
 * En un despliegue con varias instancias se reemplazará por Redis o Bucket4j distribuido.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private record Attempt(int count, Instant blockedUntil) {
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil() == null) {
            return false;
        }
        if (attempt.blockedUntil().isAfter(Instant.now())) {
            return true;
        }
        attempts.remove(key);
        return false;
    }

    public long retryAfterSeconds(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil() == null) {
            return 0;
        }
        long seconds = Duration.between(Instant.now(), attempt.blockedUntil()).toSeconds();
        return Math.max(seconds, 0);
    }

    public void onFailure(String key) {
        attempts.compute(key, (k, current) -> {
            int count = current == null ? 1 : current.count() + 1;
            Instant blockedUntil = count >= MAX_ATTEMPTS ? Instant.now().plus(BLOCK_DURATION) : null;
            return new Attempt(count, blockedUntil);
        });
    }

    public void onSuccess(String key) {
        attempts.remove(key);
    }
}
