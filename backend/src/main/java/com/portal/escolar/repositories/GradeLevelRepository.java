package com.portal.escolar.repositories;

import com.portal.escolar.entities.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GradeLevelRepository extends JpaRepository<GradeLevel, UUID> {

    List<GradeLevel> findAllByOrderBySortOrderAsc();
}
