package com.portal.escolar.repositories;

import com.portal.escolar.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByAcademicYearId(UUID academicYearId);
}
