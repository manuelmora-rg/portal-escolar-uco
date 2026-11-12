package com.portal.escolar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class AdminDtos {

    public record AdminStudentDto(
            UUID userId,
            UUID studentId,
            String fullName,
            String email,
            String documentNumber,
            String gradeLevel,
            String groupName,
            String status,
            boolean enabled
    ) {
    }

    public record AdminTeacherDto(
            UUID userId,
            UUID teacherId,
            String fullName,
            String email,
            String employeeCode,
            String specialty,
            boolean enabled
    ) {
    }

    public record AdminParentDto(
            UUID userId,
            UUID parentId,
            String fullName,
            String email,
            String documentNumber,
            String phone,
            long children
    ) {
    }

    public record AdminEnrollmentDto(
            UUID id,
            String studentName,
            String courseName,
            String academicYear,
            String status,
            LocalDate enrollmentDate
    ) {
    }

    public record AdminNewsDto(
            UUID id,
            String slug,
            String title,
            String summary,
            String content,
            String imageUrl,
            String category,
            String status,
            Instant publishedAt,
            String authorName
    ) {
    }

    public record NewsRequest(
            @NotBlank(message = "El título es obligatorio")
            @Size(max = 200)
            String title,

            @Size(max = 500)
            String summary,

            @NotBlank(message = "El contenido es obligatorio")
            String content,

            @Size(max = 500)
            String imageUrl,

            @Size(max = 40)
            String category,

            boolean publish
    ) {
    }

    public record ContactMessageDto(
            UUID id,
            String fullName,
            String email,
            String phone,
            String subject,
            String message,
            String status,
            Instant createdAt
    ) {
    }

    public record ContactStatusRequest(
            @NotBlank(message = "El estado es obligatorio")
            String status
    ) {
    }

    public record ParentLinkRequest(
            @NotBlank(message = "El correo del acudiente es obligatorio")
            @Email(message = "El correo no es válido")
            String parentEmail,

            @NotBlank(message = "El documento del estudiante es obligatorio")
            String studentDocument,

            @NotBlank(message = "El parentesco es obligatorio")
            String relationship
    ) {
    }

    // ===== Gestión de usuarios =====

    public record AdminUserDto(
            UUID id,
            String firstName,
            String lastName,
            String fullName,
            String email,
            String role,
            boolean enabled,
            boolean mustChangePassword,
            Instant lastLoginAt,
            Instant createdAt
    ) {
    }

    public record CreateUserRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80)
            String firstName,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 80)
            String lastName,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 160)
            String email,

            @NotBlank(message = "El rol es obligatorio")
            String role,

            @Size(max = 40)
            String documentNumber,

            @Size(max = 30)
            String phone,

            @Size(max = 30)
            String employeeCode,

            @Size(max = 120)
            String specialty,

            @Size(min = 10, max = 72, message = "La contraseña temporal debe tener entre 10 y 72 caracteres")
            String temporaryPassword
    ) {
    }

    public record CreatedUserDto(AdminUserDto user, String temporaryPassword) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80)
            String firstName,

            @NotBlank(message = "El apellido es obligatorio")
            @Size(max = 80)
            String lastName,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 160)
            String email
    ) {
    }

    public record UserStatusRequest(boolean enabled) {
    }

    public record ResetPasswordRequest(
            @Size(min = 10, max = 72, message = "La contraseña temporal debe tener entre 10 y 72 caracteres")
            String temporaryPassword
    ) {
    }

    public record PasswordResetDto(String temporaryPassword) {
    }

    // ===== Invitaciones =====

    public record InvitationRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 160)
            String email,

            @NotBlank(message = "El rol es obligatorio")
            String role
    ) {
    }

    public record InvitationDto(
            UUID id,
            String email,
            String role,
            Instant expiresAt,
            Instant usedAt,
            Instant revokedAt,
            Instant createdAt,
            String createdByName
    ) {
    }

    public record InvitationCreatedDto(InvitationDto invitation, String token) {
    }

    public record InvitationInfoDto(
            String email,
            String role,
            Instant expiresAt,
            boolean valid
    ) {
    }

    // ===== Auditoría =====

    public record AuditLogDto(
            UUID id,
            String actorName,
            String action,
            String entity,
            String entityId,
            String details,
            String ip,
            Instant createdAt
    ) {
    }

    private AdminDtos() {
    }
}
