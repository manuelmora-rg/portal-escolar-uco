package com.portal.escolar.entities;

import com.portal.escolar.entities.DomainEnums.ObservationType;
import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
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

import java.util.UUID;

@Entity
@Table(name = "observation")
@Getter
@Setter
@NoArgsConstructor
public class Observation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "course_subject_id")
    private UUID courseSubjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationType type = ObservationType.ACADEMIC;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationVisibility visibility = ObservationVisibility.PARENT;
}
