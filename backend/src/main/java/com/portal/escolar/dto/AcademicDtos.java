package com.portal.escolar.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class AcademicDtos {

    public record PeriodDto(
            UUID id,
            String name,
            int order,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal weight
    ) {
    }

    public record SubjectDto(UUID id, String code, String name, String area) {
    }

    public record GradeItemDto(
            UUID id,
            String title,
            String type,
            BigDecimal score,
            BigDecimal weight,
            Instant date,
            String comment
    ) {
    }

    public record SubjectGradesDto(
            UUID courseSubjectId,
            UUID subjectId,
            String subjectName,
            String teacherName,
            List<GradeItemDto> grades,
            BigDecimal average,
            BigDecimal maxScore
    ) {
    }

    public record ScheduleItemDto(
            UUID id,
            int dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String subjectName,
            String teacherName,
            String classroom
    ) {
    }

    public record SubmissionDto(
            UUID id,
            Instant submittedAt,
            String fileUrl,
            String comment,
            BigDecimal score,
            String feedback,
            String status
    ) {
    }

    public record AssignmentDto(
            UUID id,
            UUID courseSubjectId,
            String subjectName,
            String title,
            String description,
            Instant dueAt,
            BigDecimal maxScore,
            String status,
            String attachmentUrl,
            SubmissionDto submission
    ) {
    }

    public record MaterialDto(
            UUID id,
            UUID courseSubjectId,
            String subjectName,
            String title,
            String description,
            String type,
            String url,
            Instant publishedAt
    ) {
    }

    public record AttendanceItemDto(
            UUID id,
            LocalDate date,
            String status,
            String justification
    ) {
    }

    public record AttendanceSummaryDto(
            int present,
            int absent,
            int late,
            int excused,
            int total,
            BigDecimal rate
    ) {
    }

    public record AttendanceResponse(
            List<AttendanceItemDto> records,
            AttendanceSummaryDto summary
    ) {
    }

    public record ObservationDto(
            UUID id,
            String type,
            String description,
            String visibility,
            String teacherName,
            Instant createdAt
    ) {
    }

    public record NotificationDto(
            UUID id,
            String title,
            String message,
            String type,
            String link,
            Instant createdAt,
            boolean read
    ) {
    }

    public record StudentSummaryDto(
            UUID id,
            String fullName,
            String documentNumber,
            String gradeLevel,
            String groupName,
            String photoUrl,
            String status
    ) {
    }

    public record CourseDto(
            UUID id,
            String name,
            String gradeLevel,
            String group,
            long students,
            UUID homeroomTeacherId
    ) {
    }

    public record CourseSubjectDto(
            UUID id,
            UUID courseId,
            String courseName,
            UUID subjectId,
            String subjectName,
            String teacherName,
            int weeklyHours
    ) {
    }

    public record ChildDto(
            UUID studentId,
            String fullName,
            String documentNumber,
            String gradeLevel,
            String groupName,
            String relationship,
            String photoUrl
    ) {
    }

    public record TeacherStudentDto(
            UUID studentId,
            UUID enrollmentId,
            String fullName,
            String documentNumber,
            String email,
            String status
    ) {
    }

    public record PublicInstitutionDto(
            String name,
            String shortName,
            String slogan,
            String mission,
            String vision,
            String history,
            String address,
            String phone,
            String email,
            String primaryColor,
            String accentColor,
            String logoUrl
    ) {
    }

    public record NewsSummaryDto(
            UUID id,
            String slug,
            String title,
            String summary,
            String imageUrl,
            String category,
            Instant publishedAt
    ) {
    }

    public record NewsDetailDto(
            UUID id,
            String slug,
            String title,
            String summary,
            String content,
            String imageUrl,
            String category,
            Instant publishedAt,
            String authorName
    ) {
    }

    public record EventDto(
            UUID id,
            String title,
            String description,
            Instant startsAt,
            Instant endsAt,
            boolean allDay,
            String location,
            String type
    ) {
    }

    public record GalleryImageDto(UUID id, String imageUrl, String caption) {
    }

    public record GalleryAlbumDto(
            UUID id,
            String title,
            String description,
            String coverUrl,
            List<GalleryImageDto> images
    ) {
    }

    public record MeetingDto(
            UUID id,
            String title,
            String description,
            Instant scheduledAt,
            String location,
            String status,
            String courseName,
            String teacherName,
            String confirmationStatus
    ) {
    }

    public record DashboardStudentDto(
            StudentSummaryDto student,
            BigDecimal average,
            BigDecimal attendanceRate,
            long pendingAssignments,
            long unreadNotifications,
            List<AssignmentDto> upcomingAssignments
    ) {
    }

    public record DashboardTeacherDto(
            String teacherName,
            long courses,
            long students,
            long pendingSubmissions,
            List<CourseSubjectDto> courseSubjects
    ) {
    }

    public record DashboardParentDto(
            String parentName,
            List<ChildDto> children,
            long unreadNotifications
    ) {
    }

    public record DashboardAdminDto(
            long students,
            long teachers,
            long parents,
            long courses,
            long enrollments,
            long publishedNews,
            List<NewsSummaryDto> latestNews
    ) {
    }

    private AcademicDtos() {
    }
}
