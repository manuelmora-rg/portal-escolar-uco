package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.AssignmentDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceItemDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceResponse;
import com.portal.escolar.dto.AcademicDtos.AttendanceSummaryDto;
import com.portal.escolar.dto.AcademicDtos.DashboardStudentDto;
import com.portal.escolar.dto.AcademicDtos.GradeItemDto;
import com.portal.escolar.dto.AcademicDtos.MaterialDto;
import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.dto.AcademicDtos.ObservationDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.AcademicDtos.ScheduleItemDto;
import com.portal.escolar.dto.AcademicDtos.StudentSummaryDto;
import com.portal.escolar.dto.AcademicDtos.SubjectGradesDto;
import com.portal.escolar.dto.AcademicDtos.SubmissionDto;
import com.portal.escolar.dto.StudentDtos.SubmissionRequest;
import com.portal.escolar.entities.AcademicPeriod;
import com.portal.escolar.entities.AcademicYear;
import com.portal.escolar.entities.Assignment;
import com.portal.escolar.entities.AttendanceRecord;
import com.portal.escolar.entities.CourseSubject;
import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
import com.portal.escolar.entities.DomainEnums.AttendanceStatus;
import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
import com.portal.escolar.entities.DomainEnums.SubmissionStatus;
import com.portal.escolar.entities.Enrollment;
import com.portal.escolar.entities.Grade;
import com.portal.escolar.entities.Material;
import com.portal.escolar.entities.Notification;
import com.portal.escolar.entities.Observation;
import com.portal.escolar.entities.ScheduleBlock;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.Submission;
import com.portal.escolar.exceptions.BusinessException;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.AcademicPeriodRepository;
import com.portal.escolar.repositories.AcademicYearRepository;
import com.portal.escolar.repositories.AssignmentRepository;
import com.portal.escolar.repositories.AttendanceRecordRepository;
import com.portal.escolar.repositories.CourseSubjectRepository;
import com.portal.escolar.repositories.EnrollmentRepository;
import com.portal.escolar.repositories.GradeRepository;
import com.portal.escolar.repositories.MaterialRepository;
import com.portal.escolar.repositories.NotificationRepository;
import com.portal.escolar.repositories.ObservationRepository;
import com.portal.escolar.repositories.ScheduleBlockRepository;
import com.portal.escolar.repositories.SubmissionRepository;
import com.portal.escolar.security.AccessGuard;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final AccessGuard accessGuard;
    private final LookupService lookup;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicPeriodRepository periodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final GradeRepository gradeRepository;
    private final ScheduleBlockRepository scheduleRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final MaterialRepository materialRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ObservationRepository observationRepository;
    private final NotificationRepository notificationRepository;

    public record StudentContext(StudentProfile student, Enrollment enrollment, List<CourseSubject> courseSubjects) {
    }

    public StudentContext context(UserPrincipal principal) {
        StudentProfile student = accessGuard.requireOwnStudent(principal);
        AcademicYear year = academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException("No hay un año académico activo"));
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndAcademicYearId(student.getId(), year.getId())
                .orElseThrow(() -> new NotFoundException("No tienes una matrícula activa"));
        List<CourseSubject> courseSubjects = courseSubjectRepository.findByCourseId(enrollment.getCourseId());
        return new StudentContext(student, enrollment, courseSubjects);
    }

    public StudentSummaryDto summary(StudentProfile student, Enrollment enrollment) {
        return new StudentSummaryDto(
                student.getId(),
                lookup.studentName(student.getId()),
                student.getDocumentNumber(),
                lookup.courseGradeLevel(enrollment.getCourseId()),
                lookup.courseGroup(enrollment.getCourseId()),
                student.getPhotoUrl(),
                student.getStatus().name()
        );
    }

    public List<PeriodDto> periods() {
        AcademicYear year = academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException("No hay un año académico activo"));
        return periodRepository.findByAcademicYearIdOrderBySortOrderAsc(year.getId()).stream()
                .map(this::toPeriod)
                .toList();
    }

    public DashboardStudentDto dashboard(UserPrincipal principal) {
        StudentContext ctx = context(principal);
        List<SubjectGradesDto> grades = grades(principal, null);
        BigDecimal average = averageOfSubjects(grades);

        LocalDate to = LocalDate.now();
        AttendanceResponse attendance = attendance(principal, to.minusDays(30), to);

        List<AssignmentDto> assignments = assignments(principal);
        long pending = assignments.stream()
                .filter(a -> a.submission() == null)
                .count();
        List<AssignmentDto> upcoming = assignments.stream()
                .filter(a -> a.submission() == null)
                .sorted(Comparator.comparing(AssignmentDto::dueAt))
                .limit(4)
                .toList();
        long unread = notificationRepository.countByUserIdAndReadAtIsNull(principal.id());

        return new DashboardStudentDto(
                summary(ctx.student(), ctx.enrollment()),
                average,
                attendance.summary().rate(),
                pending,
                unread,
                upcoming
        );
    }

    public List<SubjectGradesDto> grades(UserPrincipal principal, UUID periodId) {
        StudentContext ctx = context(principal);
        List<Grade> all = gradeRepository.findByEnrollmentIdOrderByCreatedAtDesc(ctx.enrollment().getId());
        List<Grade> filtered = periodId == null ? all
                : all.stream().filter(grade -> grade.getPeriodId().equals(periodId)).toList();

        Map<UUID, List<Grade>> byCourseSubject = filtered.stream()
                .collect(Collectors.groupingBy(Grade::getCourseSubjectId));

        return ctx.courseSubjects().stream().map(cs -> {
            List<Grade> grades = byCourseSubject.getOrDefault(cs.getId(), List.of());
            List<GradeItemDto> items = grades.stream()
                    .sorted(Comparator.comparing(Grade::getCreatedAt).reversed())
                    .map(this::toGradeItem)
                    .toList();
            return new SubjectGradesDto(
                    cs.getId(),
                    cs.getSubjectId(),
                    lookup.subjectName(cs.getSubjectId()),
                    lookup.teacherName(cs.getTeacherId()),
                    items,
                    weightedAverage(grades),
                    BigDecimal.valueOf(5.00)
            );
        }).toList();
    }

    public List<ScheduleItemDto> schedule(UserPrincipal principal) {
        StudentContext ctx = context(principal);
        List<UUID> ids = ctx.courseSubjects().stream().map(CourseSubject::getId).toList();
        Map<UUID, CourseSubject> byId = ctx.courseSubjects().stream()
                .collect(Collectors.toMap(CourseSubject::getId, Function.identity()));

        return scheduleRepository.findByCourseSubjectIdInOrderByDayOfWeekAscStartTimeAsc(ids).stream()
                .map(block -> {
                    CourseSubject cs = byId.get(block.getCourseSubjectId());
                    return new ScheduleItemDto(
                            block.getId(),
                            block.getDayOfWeek(),
                            block.getStartTime(),
                            block.getEndTime(),
                            lookup.subjectName(cs.getSubjectId()),
                            lookup.teacherName(cs.getTeacherId()),
                            block.getClassroom()
                    );
                })
                .toList();
    }

    public List<AssignmentDto> assignments(UserPrincipal principal) {
        StudentContext ctx = context(principal);
        List<UUID> ids = ctx.courseSubjects().stream().map(CourseSubject::getId).toList();
        List<Assignment> assignments = assignmentRepository
                .findByCourseSubjectIdInAndStatusOrderByDueAtAsc(ids, AssignmentStatus.PUBLISHED);
        Map<UUID, CourseSubject> byId = ctx.courseSubjects().stream()
                .collect(Collectors.toMap(CourseSubject::getId, Function.identity()));

        return assignments.stream().map(assignment -> {
            CourseSubject cs = byId.get(assignment.getCourseSubjectId());
            Submission submission = submissionRepository
                    .findByAssignmentIdAndStudentId(assignment.getId(), ctx.student().getId())
                    .orElse(null);
            return new AssignmentDto(
                    assignment.getId(),
                    assignment.getCourseSubjectId(),
                    lookup.subjectName(cs.getSubjectId()),
                    assignment.getTitle(),
                    assignment.getDescription(),
                    assignment.getDueAt(),
                    assignment.getMaxScore(),
                    assignment.getStatus().name(),
                    assignment.getAttachmentUrl(),
                    submission == null ? null : toSubmission(submission)
            );
        }).toList();
    }

    public List<MaterialDto> materials(UserPrincipal principal) {
        StudentContext ctx = context(principal);
        List<UUID> ids = ctx.courseSubjects().stream().map(CourseSubject::getId).toList();
        Map<UUID, CourseSubject> byId = ctx.courseSubjects().stream()
                .collect(Collectors.toMap(CourseSubject::getId, Function.identity()));
        return materialRepository.findByCourseSubjectIdInOrderByPublishedAtDesc(ids).stream()
                .map(material -> {
                    CourseSubject cs = byId.get(material.getCourseSubjectId());
                    return new MaterialDto(material.getId(), material.getCourseSubjectId(),
                            lookup.subjectName(cs.getSubjectId()), material.getTitle(), material.getDescription(),
                            material.getType().name(), material.getUrl(), material.getPublishedAt());
                })
                .toList();
    }

    public AttendanceResponse attendance(UserPrincipal principal, LocalDate from, LocalDate to) {
        StudentContext ctx = context(principal);
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(30) : from;
        List<AttendanceRecord> records = attendanceRepository
                .findByEnrollmentIdInAndAttendanceDateBetween(List.of(ctx.enrollment().getId()), start, end);

        List<AttendanceItemDto> items = records.stream()
                .sorted(Comparator.comparing(AttendanceRecord::getAttendanceDate).reversed())
                .map(record -> new AttendanceItemDto(record.getId(), record.getAttendanceDate(),
                        record.getStatus().name(), record.getJustification()))
                .toList();

        int present = count(records, AttendanceStatus.PRESENT);
        int absent = count(records, AttendanceStatus.ABSENT);
        int late = count(records, AttendanceStatus.LATE);
        int excused = count(records, AttendanceStatus.EXCUSED);
        int total = records.size();
        BigDecimal rate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(present + late)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);

        return new AttendanceResponse(items, new AttendanceSummaryDto(present, absent, late, excused, total, rate));
    }

    public List<ObservationDto> observations(UserPrincipal principal) {
        StudentContext ctx = context(principal);
        return observationRepository.findByStudentIdAndVisibilityInOrderByCreatedAtDesc(
                        ctx.student().getId(), List.of(ObservationVisibility.STUDENT, ObservationVisibility.PARENT))
                .stream()
                .map(observation -> new ObservationDto(
                        observation.getId(),
                        observation.getType().name(),
                        observation.getDescription(),
                        observation.getVisibility().name(),
                        lookup.teacherName(observation.getTeacherId()),
                        observation.getCreatedAt()
                ))
                .toList();
    }

    public List<NotificationDto> notifications(UserPrincipal principal) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.id()).stream()
                .map(this::toNotification)
                .toList();
    }

    @Transactional
    public void markNotificationRead(UserPrincipal principal, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, principal.id())
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public SubmissionDto submit(UserPrincipal principal, UUID assignmentId, SubmissionRequest request) {
        StudentContext ctx = context(principal);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));

        boolean belongs = ctx.courseSubjects().stream()
                .anyMatch(cs -> cs.getId().equals(assignment.getCourseSubjectId()));
        if (!belongs) {
            throw new org.springframework.security.access.AccessDeniedException("La tarea no pertenece a tus cursos");
        }
        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new BusinessException("La tarea no está abierta para entregas");
        }

        Submission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, ctx.student().getId())
                .orElseGet(Submission::new);
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(ctx.student().getId());
        submission.setSubmittedAt(Instant.now());
        submission.setComment(request.comment());
        submission.setFileUrl(request.fileUrl());
        submission.setStatus(Instant.now().isAfter(assignment.getDueAt())
                ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED);
        return toSubmission(submissionRepository.save(submission));
    }

    private BigDecimal averageOfSubjects(List<SubjectGradesDto> grades) {
        List<BigDecimal> averages = grades.stream()
                .map(SubjectGradesDto::average)
                .filter(value -> value != null)
                .toList();
        if (averages.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = averages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(averages.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal weightedAverage(List<Grade> grades) {
        if (grades.isEmpty()) {
            return null;
        }
        BigDecimal weightSum = grades.stream()
                .map(Grade::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weightSum.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal weighted = grades.stream()
                .map(grade -> grade.getScore().multiply(grade.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weighted.divide(weightSum, 1, RoundingMode.HALF_UP);
    }

    private GradeItemDto toGradeItem(Grade grade) {
        return new GradeItemDto(grade.getId(), grade.getTitle(), grade.getType().name(), grade.getScore(),
                grade.getWeight(), grade.getCreatedAt(), grade.getComment());
    }

    private SubmissionDto toSubmission(Submission submission) {
        return new SubmissionDto(submission.getId(), submission.getSubmittedAt(), submission.getFileUrl(),
                submission.getComment(), submission.getScore(), submission.getFeedback(), submission.getStatus().name());
    }

    private NotificationDto toNotification(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getTitle(), notification.getMessage(),
                notification.getType().name(), notification.getLink(), notification.getCreatedAt(),
                notification.getReadAt() != null);
    }

    private PeriodDto toPeriod(AcademicPeriod period) {
        return new PeriodDto(period.getId(), period.getName(), period.getSortOrder(),
                period.getStartDate(), period.getEndDate(), period.getWeight());
    }

    private int count(List<AttendanceRecord> records, AttendanceStatus status) {
        return (int) records.stream().filter(record -> record.getStatus() == status).count();
    }
}
