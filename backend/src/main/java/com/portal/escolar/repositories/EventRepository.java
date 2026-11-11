package com.portal.escolar.repositories;

import com.portal.escolar.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByPublishedTrueOrderByStartsAtAsc();

    List<Event> findByPublishedTrueAndStartsAtBetweenOrderByStartsAtAsc(Instant from, Instant to);
}
