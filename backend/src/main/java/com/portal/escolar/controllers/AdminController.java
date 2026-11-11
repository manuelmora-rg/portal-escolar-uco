package com.portal.escolar.controllers;

import com.portal.escolar.dto.AcademicDtos.CourseDto;
import com.portal.escolar.dto.AcademicDtos.DashboardAdminDto;
import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.dto.AcademicDtos.PublicInstitutionDto;
import com.portal.escolar.dto.AcademicDtos.SubjectDto;
import com.portal.escolar.dto.AdminDtos.AdminEnrollmentDto;
import com.portal.escolar.dto.AdminDtos.AdminNewsDto;
import com.portal.escolar.dto.AdminDtos.AdminParentDto;
import com.portal.escolar.dto.AdminDtos.AdminStudentDto;
import com.portal.escolar.dto.AdminDtos.AdminTeacherDto;
import com.portal.escolar.dto.AdminDtos.ContactMessageDto;
import com.portal.escolar.dto.AdminDtos.ContactStatusRequest;
import com.portal.escolar.dto.AdminDtos.NewsRequest;
import com.portal.escolar.dto.AdminDtos.ParentLinkRequest;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.services.AdminService;
import com.portal.escolar.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAdminDto> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }

    @GetMapping("/students")
    public ResponseEntity<List<AdminStudentDto>> students() {
        return ResponseEntity.ok(adminService.students());
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<AdminTeacherDto>> teachers() {
        return ResponseEntity.ok(adminService.teachers());
    }

    @GetMapping("/parents")
    public ResponseEntity<List<AdminParentDto>> parents() {
        return ResponseEntity.ok(adminService.parents());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> courses() {
        return ResponseEntity.ok(adminService.courses());
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDto>> subjects() {
        return ResponseEntity.ok(adminService.subjects());
    }

    @GetMapping("/enrollments")
    public ResponseEntity<List<AdminEnrollmentDto>> enrollments() {
        return ResponseEntity.ok(adminService.enrollments());
    }

    @GetMapping("/news")
    public ResponseEntity<List<AdminNewsDto>> news() {
        return ResponseEntity.ok(adminService.news());
    }

    @PostMapping("/news")
    public ResponseEntity<AdminNewsDto> createNews(@Valid @RequestBody NewsRequest request) {
        return ResponseEntity.ok(adminService.createNews(CurrentUser.require(), request));
    }

    @PutMapping("/news/{newsId}")
    public ResponseEntity<AdminNewsDto> updateNews(@PathVariable UUID newsId,
                                                   @Valid @RequestBody NewsRequest request) {
        return ResponseEntity.ok(adminService.updateNews(CurrentUser.require(), newsId, request));
    }

    @PatchMapping("/news/{newsId}/publish")
    public ResponseEntity<AdminNewsDto> publishNews(@PathVariable UUID newsId) {
        return ResponseEntity.ok(adminService.publishNews(newsId));
    }

    @GetMapping("/contact-messages")
    public ResponseEntity<List<ContactMessageDto>> contactMessages() {
        return ResponseEntity.ok(adminService.contactMessages());
    }

    @PatchMapping("/contact-messages/{messageId}/status")
    public ResponseEntity<ContactMessageDto> updateContactStatus(@PathVariable UUID messageId,
                                                                 @Valid @RequestBody ContactStatusRequest request) {
        return ResponseEntity.ok(adminService.updateContactStatus(messageId, request));
    }

    @GetMapping("/settings/institution")
    public ResponseEntity<PublicInstitutionDto> institution() {
        return ResponseEntity.ok(adminService.institution());
    }

    @PutMapping("/settings/institution")
    public ResponseEntity<PublicInstitutionDto> updateInstitution(@RequestBody Map<String, Object> values) {
        return ResponseEntity.ok(adminService.updateInstitution(values));
    }

    @PostMapping("/parent-links")
    public ResponseEntity<Map<String, String>> linkParent(@Valid @RequestBody ParentLinkRequest request) {
        adminService.linkParent(request);
        return ResponseEntity.ok(Map.of("message", "Vínculo familiar registrado"));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications() {
        return ResponseEntity.ok(notificationService.list(CurrentUser.require()));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable UUID notificationId) {
        notificationService.markRead(CurrentUser.require(), notificationId);
        return ResponseEntity.ok(Map.of("message", "Notificación marcada como leída"));
    }
}
