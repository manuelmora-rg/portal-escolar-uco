package com.portal.escolar.repositories;

import com.portal.escolar.entities.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}
