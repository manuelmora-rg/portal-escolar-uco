package com.portal.escolar.controllers;

import com.portal.escolar.dto.AcademicDtos.EventDto;
import com.portal.escolar.dto.AcademicDtos.GalleryAlbumDto;
import com.portal.escolar.dto.AcademicDtos.NewsDetailDto;
import com.portal.escolar.dto.AcademicDtos.NewsSummaryDto;
import com.portal.escolar.dto.AcademicDtos.PublicInstitutionDto;
import com.portal.escolar.dto.PublicDtos.AcademicOfferDto;
import com.portal.escolar.dto.PublicDtos.ContactRequest;
import com.portal.escolar.exceptions.TooManyRequestsException;
import com.portal.escolar.services.PublicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;
    private final Map<String, Instant> lastContactByIp = new ConcurrentHashMap<>();

    @GetMapping("/institution")
    public ResponseEntity<PublicInstitutionDto> institution() {
        return ResponseEntity.ok(publicService.institution());
    }

    @GetMapping("/academic-offer")
    public ResponseEntity<AcademicOfferDto> academicOffer() {
        return ResponseEntity.ok(publicService.academicOffer());
    }

    @GetMapping("/news")
    public ResponseEntity<List<NewsSummaryDto>> news() {
        return ResponseEntity.ok(publicService.news());
    }

    @GetMapping("/news/{slug}")
    public ResponseEntity<NewsDetailDto> newsBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(publicService.newsBySlug(slug));
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDto>> events(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(publicService.events(from, to));
    }

    @GetMapping("/gallery")
    public ResponseEntity<List<GalleryAlbumDto>> gallery() {
        return ResponseEntity.ok(publicService.gallery());
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactRequest request,
                                                       HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        Instant last = lastContactByIp.get(ip);
        if (last != null && Duration.between(last, Instant.now()).toSeconds() < 60) {
            throw new TooManyRequestsException("Espera un minuto antes de enviar otro mensaje.");
        }
        lastContactByIp.put(ip, Instant.now());
        publicService.contact(request);
        return ResponseEntity.ok(Map.of("message", "Mensaje recibido. Gracias por contactarnos."));
    }
}
