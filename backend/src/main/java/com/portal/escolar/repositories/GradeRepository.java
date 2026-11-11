package com.portal.escolar.repositories;

import com.portal.escolar.entities.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findByEnrollmentIdOrderByCreatedAtDesc(UUID enrollmentId);

    List<Grade> findByCourseSubjectIdAndPeriodId(UUID courseSubjectId, UUID periodId);

    List<Grade> findByEnrollmentIdInAndPeriodId(List<UUID> enrollmentIds, UUID periodId);

    List<Grade> findByCourseSubjectId(UUID courseSubjectId);

    long countByCourseSubjectId(UUID courseSubjectId);
}
