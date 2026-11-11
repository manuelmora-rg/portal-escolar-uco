package com.portal.escolar.repositories;

import com.portal.escolar.entities.MeetingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingAttendanceRepository extends JpaRepository<MeetingAttendance, UUID> {

    List<MeetingAttendance> findByParentId(UUID parentId);

    Optional<MeetingAttendance> findByMeetingIdAndParentId(UUID meetingId, UUID parentId);
}
