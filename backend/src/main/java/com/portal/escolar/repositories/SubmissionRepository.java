package com.portal.escolar.repositories;

import com.portal.escolar.entities.DomainEnums.SubmissionStatus;
import com.portal.escolar.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByAssignmentIdOrderBySubmittedAtAsc(UUID assignmentId);

    Optional<Submission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    List<Submission> findByStudentId(UUID studentId);

    long countByAssignmentId(UUID assignmentId);

    long countByAssignmentIdAndStatus(UUID assignmentId, SubmissionStatus status);

    long countByAssignmentIdInAndStatusIn(Collection<UUID> assignmentIds, Collection<SubmissionStatus> statuses);
}
