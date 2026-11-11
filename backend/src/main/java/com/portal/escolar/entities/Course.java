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
@Table(name = "course", uniqueConstraints = @UniqueConstraint(columnNames = {"academic_year_id", "grade_level_id", "group_name"}))
@Getter
@Setter
@NoArgsConstructor
public class Course extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "grade_level_id", nullable = false)
    private UUID gradeLevelId;

    @Column(name = "group_name", nullable = false, length = 10)
    private String groupName;

    @Column(name = "homeroom_teacher_id")
    private UUID homeroomTeacherId;

    @Column(nullable = false)
    private Integer capacity = 35;
}
