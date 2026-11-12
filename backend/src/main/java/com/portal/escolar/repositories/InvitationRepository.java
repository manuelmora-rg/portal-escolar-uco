package com.portal.escolar.repositories;

import com.portal.escolar.entities.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByTokenHashAndUsedAtIsNullAndRevokedAtIsNull(String tokenHash);

    List<Invitation> findAllByOrderByCreatedAtDesc();

    List<Invitation> findByEmailIgnoreCaseAndUsedAtIsNullAndRevokedAtIsNull(String email);

    long countByUsedAtIsNullAndRevokedAtIsNullAndExpiresAtAfter(Instant now);
}
