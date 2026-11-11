package com.portal.escolar.repositories;

import com.portal.escolar.entities.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    Optional<AcademicYear> findByActiveTrue();

    Optional<AcademicYear> findByName(String name);
}
