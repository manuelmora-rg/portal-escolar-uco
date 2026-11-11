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

    private AdminDtos() {
    }
}
