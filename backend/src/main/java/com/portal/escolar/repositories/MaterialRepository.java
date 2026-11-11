package com.portal.escolar.repositories;

import com.portal.escolar.entities.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<Material, UUID> {

    List<Material> findByCourseSubjectIdInOrderByPublishedAtDesc(List<UUID> courseSubjectIds);

    List<Material> findByCourseSubjectIdOrderByPublishedAtDesc(UUID courseSubjectId);
}
