package com.portal.escolar.repositories;

import com.portal.escolar.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.userId = :userId and t.revoked = false")
    void revokeAllByUserId(UUID userId);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :now or t.revoked = true")
    void deleteExpiredAndRevoked(Instant now);
}
