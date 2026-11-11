package com.portal.escolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "course_subject", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "subject_id", "teacher_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CourseSubject extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "weekly_hours", nullable = false)
    private Integer weeklyHours = 4;
}
