package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor
public class Assignment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_subject_id", nullable = false)
    private UUID courseSubjectId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore = BigDecimal.valueOf(5);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.DRAFT;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
}
