package com.portal.escolar.repositories;

import com.portal.escolar.entities.ScheduleBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScheduleBlockRepository extends JpaRepository<ScheduleBlock, UUID> {

    List<ScheduleBlock> findByCourseSubjectIdInOrderByDayOfWeekAscStartTimeAsc(List<UUID> courseSubjectIds);

    List<ScheduleBlock> findByCourseSubjectIdOrderByDayOfWeekAscStartTimeAsc(UUID courseSubjectId);
}
