package com.portal.escolar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "grade_audit")
@Getter
@Setter
@NoArgsConstructor
public class GradeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "old_score", precision = 4, scale = 2)
    private BigDecimal oldScore;

    @Column(name = "new_score", precision = 4, scale = 2)
    private BigDecimal newScore;

    @Column(length = 300)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(length = 45)
    private String ip;

    @PrePersist
    void onCreate() {
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }
}
