package com.portal.escolar.repositories;

import com.portal.escolar.entities.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, UUID> {

    List<ParentStudent> findByParentIdAndAuthorizedTrue(UUID parentId);

    Optional<ParentStudent> findByParentIdAndStudentId(UUID parentId, UUID studentId);

    boolean existsByParentIdAndStudentIdAndAuthorizedTrue(UUID parentId, UUID studentId);

    List<ParentStudent> findByStudentIdAndAuthorizedTrue(UUID studentId);
}
