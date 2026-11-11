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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meeting_attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "parent_id"}))
@Getter
@Setter
@NoArgsConstructor
public class MeetingAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;

    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "confirmed_at")
    private Instant confirmedAt;
}
