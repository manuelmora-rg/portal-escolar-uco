package com.portal.escolar.repositories;

import com.portal.escolar.entities.DomainEnums.EnrollmentStatus;
import com.portal.escolar.entities.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Optional<Enrollment> findByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);

    List<Enrollment> findByCourseIdAndStatus(UUID courseId, EnrollmentStatus status);

    List<Enrollment> findByCourseId(UUID courseId);

    List<Enrollment> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);

    long countByCourseIdAndStatus(UUID courseId, EnrollmentStatus status);
}
