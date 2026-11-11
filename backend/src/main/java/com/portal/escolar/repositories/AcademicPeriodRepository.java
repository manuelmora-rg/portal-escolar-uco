package com.portal.escolar.repositories;

import com.portal.escolar.entities.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {

    List<AcademicPeriod> findByAcademicYearIdOrderBySortOrderAsc(UUID academicYearId);
}
