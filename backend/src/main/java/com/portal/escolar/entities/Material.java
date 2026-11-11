package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.MaterialType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
public class Material extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_subject_id", nullable = false)
    private UUID courseSubjectId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType type = MaterialType.LINK;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt = Instant.now();

    @Column(name = "visible_to_parents", nullable = false)
    private boolean visibleToParents = true;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
}
