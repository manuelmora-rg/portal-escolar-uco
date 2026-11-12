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
import com.portal.escolar.dto.AdminDtos.AdminUserDto;
import com.portal.escolar.dto.AdminDtos.AuditLogDto;
import com.portal.escolar.dto.AdminDtos.ContactMessageDto;
import com.portal.escolar.dto.AdminDtos.ContactStatusRequest;
import com.portal.escolar.dto.AdminDtos.CreateUserRequest;
import com.portal.escolar.dto.AdminDtos.CreatedUserDto;
import com.portal.escolar.dto.AdminDtos.InvitationCreatedDto;
import com.portal.escolar.dto.AdminDtos.InvitationDto;
import com.portal.escolar.dto.AdminDtos.InvitationRequest;
import com.portal.escolar.dto.AdminDtos.NewsRequest;
import com.portal.escolar.dto.AdminDtos.ParentLinkRequest;
import com.portal.escolar.dto.AdminDtos.PasswordResetDto;
import com.portal.escolar.dto.AdminDtos.ResetPasswordRequest;
import com.portal.escolar.dto.AdminDtos.UpdateUserRequest;
import com.portal.escolar.dto.AdminDtos.UserStatusRequest;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.services.AdminService;
import com.portal.escolar.services.AuditService;
import com.portal.escolar.services.IdentityAdminService;
import com.portal.escolar.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final IdentityAdminService identityAdminService;
    private final AuditService auditService;
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

    // ===== Gestión de usuarios =====

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> users() {
        return ResponseEntity.ok(identityAdminService.listUsers());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserDto> user(@PathVariable UUID userId) {
        return ResponseEntity.ok(identityAdminService.getUser(userId));
    }

    @PostMapping("/users")
    public ResponseEntity<CreatedUserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identityAdminService.createUser(CurrentUser.require(), request));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<AdminUserDto> updateUser(@PathVariable UUID userId,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(identityAdminService.updateUser(CurrentUser.require(), userId, request));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<AdminUserDto> updateUserStatus(@PathVariable UUID userId,
                                                         @RequestBody UserStatusRequest request) {
        return ResponseEntity.ok(identityAdminService.setStatus(CurrentUser.require(), userId, request.enabled()));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<PasswordResetDto> resetPassword(@PathVariable UUID userId,
                                                          @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(identityAdminService.resetPassword(CurrentUser.require(), userId, request));
    }

    // ===== Invitaciones =====

    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationDto>> invitations() {
        return ResponseEntity.ok(identityAdminService.listInvitations());
    }

    @PostMapping("/invitations")
    public ResponseEntity<InvitationCreatedDto> createInvitation(@Valid @RequestBody InvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(identityAdminService.createInvitation(CurrentUser.require(), request));
    }

    // ===== Auditoría =====

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDto>> auditLogs(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditService.list(limit));
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
        return ResponseEntity.ok(adminService.publishNews(CurrentUser.require(), newsId));
    }

    @GetMapping("/contact-messages")
    public ResponseEntity<List<ContactMessageDto>> contactMessages() {
        return ResponseEntity.ok(adminService.contactMessages());
    }

    @PatchMapping("/contact-messages/{messageId}/status")
    public ResponseEntity<ContactMessageDto> updateContactStatus(@PathVariable UUID messageId,
                                                                 @Valid @RequestBody ContactStatusRequest request) {
        return ResponseEntity.ok(adminService.updateContactStatus(CurrentUser.require(), messageId, request));
    }

    @GetMapping("/settings/institution")
    public ResponseEntity<PublicInstitutionDto> institution() {
        return ResponseEntity.ok(adminService.institution());
    }

    @PutMapping("/settings/institution")
    public ResponseEntity<PublicInstitutionDto> updateInstitution(@RequestBody Map<String, Object> values) {
        return ResponseEntity.ok(adminService.updateInstitution(CurrentUser.require(), values));
    }

    @PostMapping("/parent-links")
    public ResponseEntity<Map<String, String>> linkParent(@Valid @RequestBody ParentLinkRequest request) {
        adminService.linkParent(CurrentUser.require(), request);
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
