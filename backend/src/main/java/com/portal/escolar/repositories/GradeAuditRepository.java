package com.portal.escolar.repositories;

import com.portal.escolar.entities.GradeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GradeAuditRepository extends JpaRepository<GradeAudit, UUID> {

    List<GradeAudit> findByGradeIdOrderByChangedAtDesc(UUID gradeId);
}
