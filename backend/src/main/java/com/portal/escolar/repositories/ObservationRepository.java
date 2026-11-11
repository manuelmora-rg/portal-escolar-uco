package com.portal.escolar.repositories;

import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
import com.portal.escolar.entities.Observation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ObservationRepository extends JpaRepository<Observation, UUID> {

    List<Observation> findByStudentIdAndVisibilityInOrderByCreatedAtDesc(
            UUID studentId, Collection<ObservationVisibility> visibilities);

    List<Observation> findByStudentIdInAndVisibilityInOrderByCreatedAtDesc(
            List<UUID> studentIds, Collection<ObservationVisibility> visibilities);

    List<Observation> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);
}
