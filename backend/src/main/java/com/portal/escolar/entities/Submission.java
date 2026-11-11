package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.SubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "submission", uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Submission extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(length = 1000)
    private String comment;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "text")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.PENDING;
}
