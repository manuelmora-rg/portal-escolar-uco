package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.GradeType;
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
import java.util.UUID;

@Entity
@Table(name = "grade")
@Getter
@Setter
@NoArgsConstructor
public class Grade extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "course_subject_id", nullable = false)
    private UUID courseSubjectId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GradeType type = GradeType.OTHER;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight = BigDecimal.valueOf(100);

    @Column(name = "graded_by", nullable = false)
    private UUID gradedBy;

    @Column(length = 500)
    private String comment;
}
