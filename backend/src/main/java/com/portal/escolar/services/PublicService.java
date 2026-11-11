package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.EventDto;
import com.portal.escolar.dto.AcademicDtos.GalleryAlbumDto;
import com.portal.escolar.dto.AcademicDtos.GalleryImageDto;
import com.portal.escolar.dto.AcademicDtos.NewsDetailDto;
import com.portal.escolar.dto.AcademicDtos.NewsSummaryDto;
import com.portal.escolar.dto.AcademicDtos.PublicInstitutionDto;
import com.portal.escolar.dto.PublicDtos.AcademicOfferDto;
import com.portal.escolar.dto.PublicDtos.ContactRequest;
import com.portal.escolar.entities.ContactMessage;
import com.portal.escolar.entities.DomainEnums.NewsStatus;
import com.portal.escolar.entities.Event;
import com.portal.escolar.entities.GalleryAlbum;
import com.portal.escolar.entities.News;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.ContactMessageRepository;
import com.portal.escolar.repositories.EventRepository;
import com.portal.escolar.repositories.GalleryAlbumRepository;
import com.portal.escolar.repositories.GalleryImageRepository;
import com.portal.escolar.repositories.GradeLevelRepository;
import com.portal.escolar.repositories.NewsRepository;
import com.portal.escolar.repositories.SubjectRepository;
import com.portal.escolar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final SettingsService settingsService;
    private final NewsRepository newsRepository;
    private final EventRepository eventRepository;
    private final GalleryAlbumRepository galleryAlbumRepository;
    private final GalleryImageRepository galleryImageRepository;
    private final GradeLevelRepository gradeLevelRepository;
    private final SubjectRepository subjectRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;

    public PublicInstitutionDto institution() {
        return settingsService.institution();
    }

    public AcademicOfferDto academicOffer() {
        List<AcademicOfferDto.Level> levels = gradeLevelRepository.findAllByOrderBySortOrderAsc().stream()
                .map(level -> new AcademicOfferDto.Level(level.getName(), level.getSortOrder()))
                .toList();
        List<AcademicOfferDto.Subject> subjects = subjectRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(subject -> new AcademicOfferDto.Subject(subject.getCode(), subject.getName(), subject.getArea()))
                .toList();
        return new AcademicOfferDto(levels, subjects);
    }

    public List<NewsSummaryDto> news() {
        return newsRepository.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED).stream()
                .map(this::toSummary)
                .toList();
    }

    public NewsDetailDto newsBySlug(String slug) {
        News news = newsRepository.findBySlugAndStatus(slug, NewsStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));
        String author = news.getAuthorId() == null ? null
                : userRepository.findById(news.getAuthorId()).map(user -> user.getFullName()).orElse(null);
        return new NewsDetailDto(news.getId(), news.getSlug(), news.getTitle(), news.getSummary(),
                news.getContent(), news.getImageUrl(), news.getCategory(), news.getPublishedAt(), author);
    }

    public List<EventDto> events(LocalDate from, LocalDate to) {
        List<Event> events;
        if (from != null && to != null) {
            Instant start = from.atStartOfDay(BOGOTA).toInstant();
            Instant end = to.plusDays(1).atStartOfDay(BOGOTA).toInstant();
            events = eventRepository.findByPublishedTrueAndStartsAtBetweenOrderByStartsAtAsc(start, end);
        } else {
            events = eventRepository.findByPublishedTrueOrderByStartsAtAsc();
        }
        return events.stream().map(this::toEvent).toList();
    }

    public List<GalleryAlbumDto> gallery() {
        return galleryAlbumRepository.findByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::toAlbum)
                .toList();
    }

    @Transactional
    public void contact(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setFullName(request.fullName());
        message.setEmail(request.email());
        message.setPhone(request.phone());
        message.setSubject(request.subject());
        message.setMessage(request.message());
        contactMessageRepository.save(message);
    }

    private NewsSummaryDto toSummary(News news) {
        return new NewsSummaryDto(news.getId(), news.getSlug(), news.getTitle(), news.getSummary(),
                news.getImageUrl(), news.getCategory(), news.getPublishedAt());
    }

    private EventDto toEvent(Event event) {
        return new EventDto(event.getId(), event.getTitle(), event.getDescription(), event.getStartsAt(),
                event.getEndsAt(), event.isAllDay(), event.getLocation(), event.getType().name());
    }

    private GalleryAlbumDto toAlbum(GalleryAlbum album) {
        List<GalleryImageDto> images = galleryImageRepository.findByAlbumIdOrderBySortOrderAsc(album.getId()).stream()
                .map(image -> new GalleryImageDto(image.getId(), image.getImageUrl(), image.getCaption()))
                .toList();
        return new GalleryAlbumDto(album.getId(), album.getTitle(), album.getDescription(), album.getCoverUrl(), images);
    }
}
