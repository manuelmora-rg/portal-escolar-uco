package com.portal.escolar.repositories;

import com.portal.escolar.entities.CourseSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseSubjectRepository extends JpaRepository<CourseSubject, UUID> {

    List<CourseSubject> findByCourseId(UUID courseId);

    List<CourseSubject> findByCourseIdIn(List<UUID> courseIds);

    List<CourseSubject> findByTeacherId(UUID teacherId);

    boolean existsByIdAndTeacherId(UUID id, UUID teacherId);
}
