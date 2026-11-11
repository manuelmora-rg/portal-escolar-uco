package com.portal.escolar.repositories;

import com.portal.escolar.entities.ParentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParentProfileRepository extends JpaRepository<ParentProfile, UUID> {

    Optional<ParentProfile> findByUserId(UUID userId);
}
