package com.portal.escolar.repositories;

import com.portal.escolar.entities.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByCourseIdInOrderByScheduledAtDesc(List<UUID> courseIds);
}
