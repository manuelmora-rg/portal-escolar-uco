package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.AssignmentDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceItemDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceResponse;
import com.portal.escolar.dto.AcademicDtos.AttendanceSummaryDto;
import com.portal.escolar.dto.AcademicDtos.ChildDto;
import com.portal.escolar.dto.AcademicDtos.DashboardParentDto;
import com.portal.escolar.dto.AcademicDtos.GradeItemDto;
import com.portal.escolar.dto.AcademicDtos.MeetingDto;
import com.portal.escolar.dto.AcademicDtos.ObservationDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.AcademicDtos.ScheduleItemDto;
import com.portal.escolar.dto.AcademicDtos.SubjectGradesDto;
import com.portal.escolar.dto.AcademicDtos.SubmissionDto;
import com.portal.escolar.dto.ParentDtos.ChildOverviewDto;
import com.portal.escolar.dto.ParentDtos.MeetingConfirmRequest;
import com.portal.escolar.entities.AcademicPeriod;
import com.portal.escolar.entities.AcademicYear;
import com.portal.escolar.entities.Assignment;
import com.portal.escolar.entities.AttendanceRecord;
import com.portal.escolar.entities.CourseSubject;
import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
import com.portal.escolar.entities.DomainEnums.AttendanceStatus;
import com.portal.escolar.entities.DomainEnums.EnrollmentStatus;
import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
import com.portal.escolar.entities.Enrollment;
import com.portal.escolar.entities.Grade;
import com.portal.escolar.entities.Meeting;
import com.portal.escolar.entities.MeetingAttendance;
import com.portal.escolar.entities.Observation;
import com.portal.escolar.entities.ParentProfile;
import com.portal.escolar.entities.ParentStudent;
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
import com.portal.escolar.repositories.MeetingAttendanceRepository;
import com.portal.escolar.repositories.MeetingRepository;
import com.portal.escolar.repositories.ObservationRepository;
import com.portal.escolar.repositories.ParentStudentRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final AccessGuard accessGuard;
    private final LookupService lookup;
    private final NotificationService notificationService;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicPeriodRepository periodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final GradeRepository gradeRepository;
    private final ScheduleBlockRepository scheduleRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ObservationRepository observationRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;

    private record ChildContext(StudentProfile student, Enrollment enrollment, List<CourseSubject> courseSubjects,
                                ParentStudent link) {
    }

    public List<PeriodDto> periods() {
        AcademicYear year = academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException("No hay un año académico activo"));
        return periodRepository.findByAcademicYearIdOrderBySortOrderAsc(year.getId()).stream()
                .map(period -> new PeriodDto(period.getId(), period.getName(), period.getSortOrder(),
                        period.getStartDate(), period.getEndDate(), period.getWeight()))
                .toList();
    }

    public DashboardParentDto dashboard(UserPrincipal principal) {
        ParentProfile parent = accessGuard.requireParentProfile(principal);
        List<ChildDto> children = parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId()).stream()
                .map(this::toChild)
                .toList();
        String parentName = lookup.user(principal.id()) == null ? principal.fullName() : lookup.user(principal.id()).getFullName();
        return new DashboardParentDto(parentName, children, notificationService.unreadCount(principal.id()));
    }

    public List<ChildDto> children(UserPrincipal principal) {
        ParentProfile parent = accessGuard.requireParentProfile(principal);
        return parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId()).stream()
                .map(this::toChild)
                .toList();
    }

    public ChildOverviewDto overview(UserPrincipal principal, UUID studentId) {
        ChildContext ctx = context(principal, studentId);
        List<Grade> grades = gradeRepository.findByEnrollmentIdOrderByCreatedAtDesc(ctx.enrollment().getId());
        List<AssignmentDto> assignments = assignments(principal, studentId);
        long pending = assignments.stream().filter(a -> a.submission() == null).count();
        LocalDate today = LocalDate.now();
        AttendanceResponse attendance = attendance(principal, studentId, today.minusDays(30), today);

        return new ChildOverviewDto(
                ctx.student().getId(),
                lookup.studentName(ctx.student().getId()),
                ctx.student().getDocumentNumber(),
                lookup.courseGradeLevel(ctx.enrollment().getCourseId()),
                lookup.courseGroup(ctx.enrollment().getCourseId()),
                ctx.student().getPhotoUrl(),
                ctx.link().getRelationship().name(),
                weightedAverage(grades),
                attendance.summary().rate(),
                pending,
                notificationService.unreadCount(ctx.student().getUserId())
        );
    }

    public List<SubjectGradesDto> grades(UserPrincipal principal, UUID studentId, UUID periodId) {
        ChildContext ctx = context(principal, studentId);
        List<Grade> all = gradeRepository.findByEnrollmentIdOrderByCreatedAtDesc(ctx.enrollment().getId());
        List<Grade> filtered = periodId == null ? all
                : all.stream().filter(grade -> grade.getPeriodId().equals(periodId)).toList();
        Map<UUID, List<Grade>> byCourseSubject = filtered.stream()
                .collect(Collectors.groupingBy(Grade::getCourseSubjectId));

        return ctx.courseSubjects().stream().map(cs -> {
            List<Grade> grades = byCourseSubject.getOrDefault(cs.getId(), List.of());
            return new SubjectGradesDto(
                    cs.getId(),
                    cs.getSubjectId(),
                    lookup.subjectName(cs.getSubjectId()),
                    lookup.teacherName(cs.getTeacherId()),
                    grades.stream()
                            .sorted(Comparator.comparing(Grade::getCreatedAt).reversed())
                            .map(this::toGradeItem)
                            .toList(),
                    weightedAverage(grades),
                    BigDecimal.valueOf(5.00)
            );
        }).toList();
    }

    public List<ScheduleItemDto> schedule(UserPrincipal principal, UUID studentId) {
        ChildContext ctx = context(principal, studentId);
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

    public List<AssignmentDto> assignments(UserPrincipal principal, UUID studentId) {
        ChildContext ctx = context(principal, studentId);
        List<UUID> ids = ctx.courseSubjects().stream().map(CourseSubject::getId).toList();
        Map<UUID, CourseSubject> byId = ctx.courseSubjects().stream()
                .collect(Collectors.toMap(CourseSubject::getId, Function.identity()));

        return assignmentRepository.findByCourseSubjectIdInAndStatusOrderByDueAtAsc(ids, AssignmentStatus.PUBLISHED)
                .stream()
                .map(assignment -> {
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
                })
                .toList();
    }

    public AttendanceResponse attendance(UserPrincipal principal, UUID studentId, LocalDate from, LocalDate to) {
        ChildContext ctx = context(principal, studentId);
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

    public List<ObservationDto> observations(UserPrincipal principal, UUID studentId) {
        ChildContext ctx = context(principal, studentId);
        return observationRepository.findByStudentIdAndVisibilityInOrderByCreatedAtDesc(
                        ctx.student().getId(), List.of(ObservationVisibility.PARENT))
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

    public List<MeetingDto> meetings(UserPrincipal principal) {
        ParentProfile parent = accessGuard.requireParentProfile(principal);
        Set<UUID> courseIds = new LinkedHashSet<>();
        for (ParentStudent link : parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId())) {
            enrollmentFor(link.getStudentId()).ifPresent(enrollment -> courseIds.add(enrollment.getCourseId()));
        }
        if (courseIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, MeetingAttendance> attendanceByMeeting = meetingAttendanceRepository.findByParentId(parent.getId())
                .stream().collect(Collectors.toMap(MeetingAttendance::getMeetingId, Function.identity()));

        return meetingRepository.findByCourseIdInOrderByScheduledAtDesc(List.copyOf(courseIds)).stream()
                .map(meeting -> {
                    MeetingAttendance attendance = attendanceByMeeting.get(meeting.getId());
                    return new MeetingDto(
                            meeting.getId(),
                            meeting.getTitle(),
                            meeting.getDescription(),
                            meeting.getScheduledAt(),
                            meeting.getLocation(),
                            meeting.getStatus(),
                            meeting.getCourseId() == null ? "" : lookup.courseName(meeting.getCourseId()),
                            meeting.getTeacherId() == null ? "" : lookup.teacherName(meeting.getTeacherId()),
                            attendance == null ? "PENDING" : attendance.getStatus()
                    );
                })
                .toList();
    }

    @Transactional
    public MeetingDto confirmMeeting(UserPrincipal principal, UUID meetingId, MeetingConfirmRequest request) {
        ParentProfile parent = accessGuard.requireParentProfile(principal);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException("Reunión no encontrada"));

        boolean hasChildInMeeting = meeting.getCourseId() != null
                && parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId()).stream()
                .map(link -> enrollmentFor(link.getStudentId()).map(Enrollment::getCourseId).orElse(null))
                .anyMatch(courseId -> meeting.getCourseId().equals(courseId));
        if (!hasChildInMeeting) {
            throw new org.springframework.security.access.AccessDeniedException("La reunión no corresponde a tus hijos");
        }

        MeetingAttendance attendance = meetingAttendanceRepository
                .findByMeetingIdAndParentId(meetingId, parent.getId())
                .orElseGet(MeetingAttendance::new);
        attendance.setMeetingId(meetingId);
        attendance.setParentId(parent.getId());
        attendance.setStatus(request.status());
        attendance.setConfirmedAt(Instant.now());
        meetingAttendanceRepository.save(attendance);

        return new MeetingDto(meeting.getId(), meeting.getTitle(), meeting.getDescription(),
                meeting.getScheduledAt(), meeting.getLocation(), meeting.getStatus(),
                meeting.getCourseId() == null ? "" : lookup.courseName(meeting.getCourseId()),
                meeting.getTeacherId() == null ? "" : lookup.teacherName(meeting.getTeacherId()),
                attendance.getStatus());
    }

    private ChildContext context(UserPrincipal principal, UUID studentId) {
        StudentProfile student = accessGuard.requireStudentAccess(principal, studentId);
        ParentProfile parent = accessGuard.requireParentProfile(principal);
        ParentStudent link = parentStudentRepository.findByParentIdAndStudentId(parent.getId(), student.getId())
                .orElseThrow(() -> new NotFoundException("Vínculo familiar no encontrado"));
        Enrollment enrollment = enrollmentFor(student.getId())
                .orElseThrow(() -> new NotFoundException("El estudiante no tiene una matrícula activa"));
        List<CourseSubject> courseSubjects = courseSubjectRepository.findByCourseId(enrollment.getCourseId());
        return new ChildContext(student, enrollment, courseSubjects, link);
    }

    private java.util.Optional<Enrollment> enrollmentFor(UUID studentId) {
        AcademicYear year = academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException("No hay un año académico activo"));
        return enrollmentRepository.findByStudentIdAndAcademicYearId(studentId, year.getId())
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.ENROLLED);
    }

    private ChildDto toChild(ParentStudent link) {
        StudentProfile student = lookup.student(link.getStudentId());
        if (student == null) {
            throw new NotFoundException("Estudiante no encontrado");
        }
        Enrollment enrollment = enrollmentFor(student.getId()).orElse(null);
        return new ChildDto(
                student.getId(),
                lookup.studentName(student.getId()),
                student.getDocumentNumber(),
                enrollment == null ? "" : lookup.courseGradeLevel(enrollment.getCourseId()),
                enrollment == null ? "" : lookup.courseGroup(enrollment.getCourseId()),
                link.getRelationship().name(),
                student.getPhotoUrl()
        );
    }

    private BigDecimal weightedAverage(List<Grade> grades) {
        if (grades.isEmpty()) {
            return null;
        }
        BigDecimal weightSum = grades.stream().map(Grade::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private int count(List<AttendanceRecord> records, AttendanceStatus status) {
        return (int) records.stream().filter(record -> record.getStatus() == status).count();
    }
}
