package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.entities.DomainEnums.NotificationType;
import com.portal.escolar.entities.Notification;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.NotificationRepository;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Notificaciones internas por usuario. Se comparten entre todos los paneles. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDto> list(UserPrincipal principal) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.id()).stream()
                .map(this::toDto)
                .toList();
    }

    public long unreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(UserPrincipal principal, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, principal.id())
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void create(UUID userId, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getTitle(), notification.getMessage(),
                notification.getType().name(), notification.getLink(), notification.getCreatedAt(),
                notification.getReadAt() != null);
    }
}
