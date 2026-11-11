package com.portal.escolar.repositories;

import com.portal.escolar.entities.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByEnrollmentIdInAndAttendanceDateBetween(
            List<UUID> enrollmentIds, LocalDate from, LocalDate to);

    List<AttendanceRecord> findByEnrollmentIdInAndAttendanceDate(List<UUID> enrollmentIds, LocalDate date);

    Optional<AttendanceRecord> findByEnrollmentIdAndAttendanceDate(UUID enrollmentId, LocalDate date);

    List<AttendanceRecord> findByEnrollmentId(UUID enrollmentId);
}
