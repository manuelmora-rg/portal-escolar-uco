package com.portal.escolar.dto;

import com.portal.escolar.dto.AcademicDtos.GradeItemDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TeacherDtos {

    public record GradeEntry(
            @NotNull UUID enrollmentId,
            @NotNull UUID periodId,
            @NotBlank String title,
            @NotBlank String type,
            @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal score,
            @NotNull @DecimalMin("0.01") @DecimalMax("100.0") BigDecimal weight,
            String comment
    ) {
    }

    public record GradeBatchRequest(
            @NotEmpty @Valid List<GradeEntry> entries
    ) {
    }

    public record AttendanceEntry(
            @NotNull UUID enrollmentId,
            @NotBlank String status,
            String justification
    ) {
    }

    public record AttendanceBatchRequest(
            @NotNull LocalDate date,
            @NotEmpty @Valid List<AttendanceEntry> entries
    ) {
    }

    public record AssignmentCreateRequest(
            @NotBlank String title,
            String description,
            @NotNull Instant dueAt,
            @NotNull @DecimalMin("0.1") @DecimalMax("100.0") BigDecimal maxScore,
            String attachmentUrl,
            boolean publish
    ) {
    }

    public record SubmissionGradeRequest(
            @NotNull @DecimalMin("0.0") BigDecimal score,
            String feedback
    ) {
    }

    public record ObservationCreateRequest(
            @NotNull UUID studentId,
            UUID courseSubjectId,
            @NotBlank String description,
            @NotBlank String type,
            @NotBlank String visibility
    ) {
    }

    public record MaterialCreateRequest(
            @NotBlank String title,
            String description,
            @NotBlank String type,
            @NotBlank String url,
            boolean visibleToParents
    ) {
    }

    public record GradebookStudent(
            UUID enrollmentId,
            UUID studentId,
            String fullName,
            String documentNumber,
            List<GradeItemDto> grades,
            BigDecimal average
    ) {
    }

    public record GradebookResponse(
            UUID courseSubjectId,
            String courseName,
            String subjectName,
            PeriodDto period,
            List<GradebookStudent> students
    ) {
    }

    public record TeacherCourseDto(
            UUID courseId,
            String courseName,
            String gradeLevel,
            String group,
            long students,
            long subjects
    ) {
    }

    public record SubmissionDto(
            UUID id,
            UUID studentId,
            String studentName,
            Instant submittedAt,
            String fileUrl,
            String comment,
            BigDecimal score,
            String feedback,
            String status
    ) {
    }

    public record TeacherAssignmentDto(
            UUID id,
            UUID courseSubjectId,
            String courseName,
            String subjectName,
            String title,
            String description,
            Instant dueAt,
            BigDecimal maxScore,
            String status,
            long submissions,
            long graded
    ) {
    }

    public record TeacherObservationDto(
            UUID id,
            UUID studentId,
            String studentName,
            String type,
            String description,
            String visibility,
            Instant createdAt
    ) {
    }

    public record TeacherMaterialDto(
            UUID id,
            UUID courseSubjectId,
            String courseName,
            String subjectName,
            String title,
            String description,
            String type,
            String url,
            Instant publishedAt,
            boolean visibleToParents
    ) {
    }

    public record AttendanceRowDto(
            UUID enrollmentId,
            UUID studentId,
            String fullName,
            String documentNumber,
            String status,
            String justification
    ) {
    }

    public record AttendanceSheetDto(
            UUID courseId,
            String courseName,
            LocalDate date,
            List<AttendanceRowDto> rows
    ) {
    }

    private TeacherDtos() {
    }
}
