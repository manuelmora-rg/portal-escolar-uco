package com.portal.escolar.repositories;

import com.portal.escolar.entities.Assignment;
import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

    List<Assignment> findByCourseSubjectIdInAndStatusOrderByDueAtAsc(List<UUID> courseSubjectIds, AssignmentStatus status);

    List<Assignment> findByCourseSubjectIdAndStatusOrderByDueAtAsc(UUID courseSubjectId, AssignmentStatus status);

    List<Assignment> findByCourseSubjectIdInOrderByDueAtDesc(List<UUID> courseSubjectIds);
}
