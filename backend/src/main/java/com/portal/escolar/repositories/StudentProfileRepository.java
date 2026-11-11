package com.portal.escolar.repositories;

import com.portal.escolar.entities.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    Optional<StudentProfile> findByUserId(UUID userId);

    Optional<StudentProfile> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);
}
