package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.CourseSubjectDto;
import com.portal.escolar.dto.AcademicDtos.DashboardTeacherDto;
import com.portal.escolar.dto.AcademicDtos.GradeItemDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.TeacherDtos.AssignmentCreateRequest;
import com.portal.escolar.dto.TeacherDtos.AttendanceBatchRequest;
import com.portal.escolar.dto.TeacherDtos.AttendanceEntry;
import com.portal.escolar.dto.TeacherDtos.AttendanceRowDto;
import com.portal.escolar.dto.TeacherDtos.AttendanceSheetDto;
import com.portal.escolar.dto.TeacherDtos.GradeBatchRequest;
import com.portal.escolar.dto.TeacherDtos.GradeEntry;
import com.portal.escolar.dto.TeacherDtos.GradebookResponse;
import com.portal.escolar.dto.TeacherDtos.GradebookStudent;
import com.portal.escolar.dto.TeacherDtos.MaterialCreateRequest;
import com.portal.escolar.dto.TeacherDtos.ObservationCreateRequest;
import com.portal.escolar.dto.TeacherDtos.SubmissionGradeRequest;
import com.portal.escolar.dto.TeacherDtos.TeacherAssignmentDto;
import com.portal.escolar.dto.TeacherDtos.TeacherCourseDto;
import com.portal.escolar.dto.TeacherDtos.TeacherMaterialDto;
import com.portal.escolar.dto.TeacherDtos.TeacherObservationDto;
import com.portal.escolar.dto.AcademicDtos.TeacherStudentDto;
import com.portal.escolar.entities.AcademicPeriod;
import com.portal.escolar.entities.AcademicYear;
import com.portal.escolar.entities.Assignment;
import com.portal.escolar.entities.AttendanceRecord;
import com.portal.escolar.entities.CourseSubject;
import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
import com.portal.escolar.entities.DomainEnums.AttendanceStatus;
import com.portal.escolar.entities.DomainEnums.EnrollmentStatus;
import com.portal.escolar.entities.DomainEnums.GradeType;
import com.portal.escolar.entities.DomainEnums.MaterialType;
import com.portal.escolar.entities.DomainEnums.NotificationType;
import com.portal.escolar.entities.DomainEnums.ObservationType;
import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
import com.portal.escolar.entities.DomainEnums.SubmissionStatus;
import com.portal.escolar.entities.Enrollment;
import com.portal.escolar.entities.Grade;
import com.portal.escolar.entities.GradeAudit;
import com.portal.escolar.entities.Material;
import com.portal.escolar.entities.Observation;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.Submission;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.exceptions.BusinessException;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.AcademicPeriodRepository;
import com.portal.escolar.repositories.AcademicYearRepository;
import com.portal.escolar.repositories.AssignmentRepository;
import com.portal.escolar.repositories.AttendanceRecordRepository;
import com.portal.escolar.repositories.CourseRepository;
import com.portal.escolar.repositories.CourseSubjectRepository;
import com.portal.escolar.repositories.EnrollmentRepository;
import com.portal.escolar.repositories.GradeAuditRepository;
import com.portal.escolar.repositories.GradeRepository;
import com.portal.escolar.repositories.MaterialRepository;
import com.portal.escolar.repositories.ObservationRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.SubmissionRepository;
import com.portal.escolar.security.AccessGuard;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class TeacherService {

    private final AccessGuard accessGuard;
    private final LookupService lookup;
    private final NotificationService notificationService;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicPeriodRepository periodRepository;
    private final CourseRepository courseRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final GradeAuditRepository gradeAuditRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final MaterialRepository materialRepository;
    private final ObservationRepository observationRepository;
    private final StudentProfileRepository studentRepository;

    public List<PeriodDto> periods() {
        AcademicYear year = activeYear();
        return periodRepository.findByAcademicYearIdOrderBySortOrderAsc(year.getId()).stream()
                .map(this::toPeriod)
                .toList();
    }

    public DashboardTeacherDto dashboard(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        List<CourseSubject> courseSubjects = courseSubjectRepository.findByTeacherId(teacher.getId());

        Set<UUID> courseIds = courseSubjects.stream().map(CourseSubject::getCourseId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<UUID> studentIds = new LinkedHashSet<>();
        for (UUID courseId : courseIds) {
            enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED)
                    .forEach(enrollment -> studentIds.add(enrollment.getStudentId()));
        }

        List<UUID> assignmentIds = courseSubjects.isEmpty() ? List.of()
                : assignmentRepository.findByCourseSubjectIdInOrderByDueAtDesc(
                        courseSubjects.stream().map(CourseSubject::getId).toList())
                .stream().map(Assignment::getId).toList();
        long pending = assignmentIds.isEmpty() ? 0
                : submissionRepository.countByAssignmentIdInAndStatusIn(assignmentIds,
                        List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.LATE));

        return new DashboardTeacherDto(
                lookup.user(principal.id()) == null ? principal.fullName() : lookup.user(principal.id()).getFullName(),
                courseIds.size(),
                studentIds.size(),
                pending,
                courseSubjects.stream().map(this::toCourseSubjectDto).toList()
        );
    }

    public List<TeacherCourseDto> courses(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        List<CourseSubject> courseSubjects = courseSubjectRepository.findByTeacherId(teacher.getId());
        Map<UUID, List<CourseSubject>> byCourse = courseSubjects.stream()
                .collect(Collectors.groupingBy(CourseSubject::getCourseId));

        return byCourse.entrySet().stream()
                .map(entry -> new TeacherCourseDto(
                        entry.getKey(),
                        lookup.courseName(entry.getKey()),
                        lookup.courseGradeLevel(entry.getKey()),
                        lookup.courseGroup(entry.getKey()),
                        enrollmentRepository.countByCourseIdAndStatus(entry.getKey(), EnrollmentStatus.ENROLLED),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparing(TeacherCourseDto::courseName))
                .toList();
    }

    public List<CourseSubjectDto> courseSubjects(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        return courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .map(this::toCourseSubjectDto)
                .toList();
    }

    public List<TeacherStudentDto> students(UserPrincipal principal, UUID courseId) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        boolean teachesCourse = courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .anyMatch(cs -> cs.getCourseId().equals(courseId));
        if (!teachesCourse) {
            throw new AccessDeniedException("El curso no está asignado a este docente");
        }
        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED).stream()
                .map(enrollment -> new TeacherStudentDto(
                        enrollment.getStudentId(),
                        enrollment.getId(),
                        lookup.studentName(enrollment.getStudentId()),
                        lookup.student(enrollment.getStudentId()) == null ? ""
                                : lookup.student(enrollment.getStudentId()).getDocumentNumber(),
                        lookup.studentEmail(enrollment.getStudentId()),
                        enrollment.getStatus().name()
                ))
                .sorted(Comparator.comparing(TeacherStudentDto::fullName))
                .toList();
    }

    public GradebookResponse gradebook(UserPrincipal principal, UUID courseSubjectId, UUID periodId) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        AcademicPeriod period = resolvePeriod(periodId);
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndStatus(cs.getCourseId(), EnrollmentStatus.ENROLLED);
        List<UUID> enrollmentIds = enrollments.stream().map(Enrollment::getId).toList();

        List<Grade> grades = enrollmentIds.isEmpty() ? List.of()
                : gradeRepository.findByEnrollmentIdInAndPeriodId(enrollmentIds, period.getId());
        Map<UUID, List<Grade>> byEnrollment = grades.stream()
                .collect(Collectors.groupingBy(Grade::getEnrollmentId));

        List<GradebookStudent> students = enrollments.stream()
                .map(enrollment -> {
                    List<Grade> own = byEnrollment.getOrDefault(enrollment.getId(), List.of());
                    return new GradebookStudent(
                            enrollment.getId(),
                            enrollment.getStudentId(),
                            lookup.studentName(enrollment.getStudentId()),
                            lookup.student(enrollment.getStudentId()) == null ? ""
                                    : lookup.student(enrollment.getStudentId()).getDocumentNumber(),
                            own.stream()
                                    .sorted(Comparator.comparing(Grade::getCreatedAt).reversed())
                                    .map(this::toGradeItem)
                                    .toList(),
                            weightedAverage(own)
                    );
                })
                .sorted(Comparator.comparing(GradebookStudent::fullName))
                .toList();

        return new GradebookResponse(cs.getId(), lookup.courseName(cs.getCourseId()),
                lookup.subjectName(cs.getSubjectId()), toPeriod(period), students);
    }

    @Transactional
    public GradebookResponse saveGrades(UserPrincipal principal, UUID courseSubjectId, GradeBatchRequest request) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        AcademicYear year = activeYear();

        UUID firstPeriod = null;
        for (GradeEntry entry : request.entries()) {
            Enrollment enrollment = enrollmentRepository.findById(entry.enrollmentId())
                    .orElseThrow(() -> new NotFoundException("Matrícula no encontrada"));
            if (!enrollment.getCourseId().equals(cs.getCourseId())) {
                throw new AccessDeniedException("El estudiante no pertenece al curso de esta asignatura");
            }
            AcademicPeriod period = periodRepository.findById(entry.periodId())
                    .orElseThrow(() -> new NotFoundException("Periodo no encontrado"));
            if (!period.getAcademicYearId().equals(year.getId())) {
                throw new BusinessException("El periodo no pertenece al año académico activo");
            }
            if (firstPeriod == null) {
                firstPeriod = period.getId();
            }

            Grade grade = new Grade();
            grade.setEnrollmentId(enrollment.getId());
            grade.setCourseSubjectId(cs.getId());
            grade.setPeriodId(period.getId());
            grade.setTitle(entry.title().trim());
            grade.setType(parseGradeType(entry.type()));
            grade.setScore(entry.score());
            grade.setWeight(entry.weight());
            grade.setGradedBy(principal.id());
            grade.setComment(entry.comment());
            gradeRepository.save(grade);

            StudentProfile student = studentRepository.findById(enrollment.getStudentId()).orElse(null);
            if (student != null) {
                notificationService.create(student.getUserId(), "Nueva calificación",
                        "Se registró una nota en " + lookup.subjectName(cs.getSubjectId()) + ": " + grade.getTitle(),
                        NotificationType.GRADE, "/app/estudiante/calificaciones");
            }
        }
        return gradebook(principal, cs.getId(), firstPeriod);
    }

    @Transactional
    public GradeItemDto updateGrade(UserPrincipal principal, UUID gradeId, GradeEntry entry, String ip) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new NotFoundException("Calificación no encontrada"));
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, grade.getCourseSubjectId());

        BigDecimal oldScore = grade.getScore();
        grade.setTitle(entry.title().trim());
        grade.setType(parseGradeType(entry.type()));
        grade.setScore(entry.score());
        grade.setWeight(entry.weight());
        grade.setComment(entry.comment());
        grade.setGradedBy(principal.id());
        gradeRepository.save(grade);

        GradeAudit audit = new GradeAudit();
        audit.setGradeId(grade.getId());
        audit.setChangedBy(principal.id());
        audit.setOldScore(oldScore);
        audit.setNewScore(entry.score());
        audit.setReason(entry.comment() == null || entry.comment().isBlank()
                ? "Actualización de calificación" : entry.comment());
        audit.setIp(ip);
        gradeAuditRepository.save(audit);

        StudentProfile student = enrollmentRepository.findById(grade.getEnrollmentId())
                .flatMap(enrollment -> studentRepository.findById(enrollment.getStudentId()))
                .orElse(null);
        if (student != null) {
            notificationService.create(student.getUserId(), "Calificación actualizada",
                    "Se modificó una nota en " + lookup.subjectName(cs.getSubjectId()) + ": " + grade.getTitle(),
                    NotificationType.GRADE, "/app/estudiante/calificaciones");
        }
        return toGradeItem(grade);
    }

    public AttendanceSheetDto attendanceSheet(UserPrincipal principal, UUID courseSubjectId, LocalDate date) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        LocalDate target = date == null ? LocalDate.now() : date;
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndStatus(cs.getCourseId(), EnrollmentStatus.ENROLLED);
        List<UUID> ids = enrollments.stream().map(Enrollment::getId).toList();
        Map<UUID, AttendanceRecord> existing = ids.isEmpty() ? Map.of()
                : attendanceRepository.findByEnrollmentIdInAndAttendanceDate(ids, target).stream()
                .collect(Collectors.toMap(AttendanceRecord::getEnrollmentId, Function.identity()));

        List<AttendanceRowDto> rows = enrollments.stream()
                .map(enrollment -> {
                    AttendanceRecord record = existing.get(enrollment.getId());
                    return new AttendanceRowDto(
                            enrollment.getId(),
                            enrollment.getStudentId(),
                            lookup.studentName(enrollment.getStudentId()),
                            lookup.student(enrollment.getStudentId()) == null ? ""
                                    : lookup.student(enrollment.getStudentId()).getDocumentNumber(),
                            record == null ? null : record.getStatus().name(),
                            record == null ? null : record.getJustification()
                    );
                })
                .sorted(Comparator.comparing(AttendanceRowDto::fullName))
                .toList();

        return new AttendanceSheetDto(cs.getCourseId(), lookup.courseName(cs.getCourseId()), target, rows);
    }

    @Transactional
    public AttendanceSheetDto saveAttendance(UserPrincipal principal, UUID courseSubjectId, AttendanceBatchRequest request) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        LocalDate date = request.date() == null ? LocalDate.now() : request.date();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndStatus(cs.getCourseId(), EnrollmentStatus.ENROLLED);
        Map<UUID, Enrollment> byId = enrollments.stream()
                .collect(Collectors.toMap(Enrollment::getId, Function.identity()));

        for (AttendanceEntry entry : request.entries()) {
            Enrollment enrollment = byId.get(entry.enrollmentId());
            if (enrollment == null) {
                throw new AccessDeniedException("El estudiante no pertenece al curso de esta asignatura");
            }
            AttendanceRecord record = attendanceRepository
                    .findByEnrollmentIdAndAttendanceDate(enrollment.getId(), date)
                    .orElseGet(AttendanceRecord::new);
            record.setEnrollmentId(enrollment.getId());
            record.setAttendanceDate(date);
            record.setStatus(parseAttendanceStatus(entry.status()));
            record.setJustification(entry.justification());
            record.setRecordedBy(principal.id());
            attendanceRepository.save(record);
        }
        return attendanceSheet(principal, cs.getId(), date);
    }

    public List<TeacherAssignmentDto> assignments(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        List<UUID> ids = courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .map(CourseSubject::getId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return assignmentRepository.findByCourseSubjectIdInOrderByDueAtDesc(ids).stream()
                .map(assignment -> {
                    CourseSubject cs = courseSubjectRepository.findById(assignment.getCourseSubjectId()).orElse(null);
                    return new TeacherAssignmentDto(
                            assignment.getId(),
                            assignment.getCourseSubjectId(),
                            cs == null ? "" : lookup.courseName(cs.getCourseId()),
                            cs == null ? "" : lookup.subjectName(cs.getSubjectId()),
                            assignment.getTitle(),
                            assignment.getDescription(),
                            assignment.getDueAt(),
                            assignment.getMaxScore(),
                            assignment.getStatus().name(),
                            submissionRepository.countByAssignmentId(assignment.getId()),
                            submissionRepository.countByAssignmentIdAndStatus(assignment.getId(), SubmissionStatus.GRADED)
                    );
                })
                .toList();
    }

    @Transactional
    public TeacherAssignmentDto createAssignment(UserPrincipal principal, UUID courseSubjectId,
                                                 AssignmentCreateRequest request) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        Assignment assignment = new Assignment();
        assignment.setCourseSubjectId(cs.getId());
        assignment.setTitle(request.title().trim());
        assignment.setDescription(request.description());
        assignment.setDueAt(request.dueAt());
        assignment.setMaxScore(request.maxScore());
        assignment.setAttachmentUrl(request.attachmentUrl());
        assignment.setStatus(request.publish() ? AssignmentStatus.PUBLISHED : AssignmentStatus.DRAFT);
        assignment.setCreatedBy(principal.id());
        assignmentRepository.save(assignment);

        if (request.publish()) {
            enrollmentRepository.findByCourseIdAndStatus(cs.getCourseId(), EnrollmentStatus.ENROLLED)
                    .forEach(enrollment -> studentRepository.findById(enrollment.getStudentId())
                            .ifPresent(student -> notificationService.create(student.getUserId(),
                                    "Nueva tarea publicada",
                                    request.title() + " - " + lookup.subjectName(cs.getSubjectId()),
                                    NotificationType.ASSIGNMENT, "/app/estudiante/tareas")));
        }

        return new TeacherAssignmentDto(assignment.getId(), cs.getId(),
                lookup.courseName(cs.getCourseId()), lookup.subjectName(cs.getSubjectId()),
                assignment.getTitle(), assignment.getDescription(), assignment.getDueAt(), assignment.getMaxScore(),
                assignment.getStatus().name(), 0, 0);
    }

    public List<com.portal.escolar.dto.TeacherDtos.SubmissionDto> submissions(UserPrincipal principal, UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        accessGuard.requireOwnCourseSubject(principal, assignment.getCourseSubjectId());

        return submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(assignmentId).stream()
                .map(submission -> new com.portal.escolar.dto.TeacherDtos.SubmissionDto(
                        submission.getId(),
                        submission.getStudentId(),
                        lookup.studentName(submission.getStudentId()),
                        submission.getSubmittedAt(),
                        submission.getFileUrl(),
                        submission.getComment(),
                        submission.getScore(),
                        submission.getFeedback(),
                        submission.getStatus().name()
                ))
                .toList();
    }

    @Transactional
    public com.portal.escolar.dto.TeacherDtos.SubmissionDto gradeSubmission(UserPrincipal principal,
                                                                            UUID submissionId,
                                                                            SubmissionGradeRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Entrega no encontrada"));
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, assignment.getCourseSubjectId());

        if (request.score().compareTo(assignment.getMaxScore()) > 0) {
            throw new BusinessException("La nota no puede superar el puntaje máximo de la tarea");
        }
        submission.setScore(request.score());
        submission.setFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submissionRepository.save(submission);

        StudentProfile student = studentRepository.findById(submission.getStudentId()).orElse(null);
        if (student != null) {
            notificationService.create(student.getUserId(), "Tarea calificada",
                    assignment.getTitle() + " fue calificada con " + request.score(),
                    NotificationType.ASSIGNMENT, "/app/estudiante/tareas");
        }

        return new com.portal.escolar.dto.TeacherDtos.SubmissionDto(
                submission.getId(), submission.getStudentId(), lookup.studentName(submission.getStudentId()),
                submission.getSubmittedAt(), submission.getFileUrl(), submission.getComment(),
                submission.getScore(), submission.getFeedback(), submission.getStatus().name());
    }

    public List<TeacherObservationDto> observations(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        return observationRepository.findByTeacherIdOrderByCreatedAtDesc(teacher.getId()).stream()
                .map(observation -> new TeacherObservationDto(
                        observation.getId(),
                        observation.getStudentId(),
                        lookup.studentName(observation.getStudentId()),
                        observation.getType().name(),
                        observation.getDescription(),
                        observation.getVisibility().name(),
                        observation.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public TeacherObservationDto createObservation(UserPrincipal principal, ObservationCreateRequest request) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        if (request.courseSubjectId() != null) {
            accessGuard.requireOwnCourseSubject(principal, request.courseSubjectId());
        }
        requireStudentInTeachersCourses(teacher, request.studentId());

        Observation observation = new Observation();
        observation.setStudentId(request.studentId());
        observation.setTeacherId(teacher.getId());
        observation.setCourseSubjectId(request.courseSubjectId());
        observation.setType(parseObservationType(request.type()));
        observation.setDescription(request.description());
        observation.setVisibility(parseObservationVisibility(request.visibility()));
        observationRepository.save(observation);

        return new TeacherObservationDto(observation.getId(), observation.getStudentId(),
                lookup.studentName(observation.getStudentId()), observation.getType().name(),
                observation.getDescription(), observation.getVisibility().name(), observation.getCreatedAt());
    }

    public List<TeacherMaterialDto> materials(UserPrincipal principal) {
        TeacherProfile teacher = accessGuard.requireTeacherProfile(principal);
        List<UUID> ids = courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .map(CourseSubject::getId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<UUID, CourseSubject> byId = courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .collect(Collectors.toMap(CourseSubject::getId, Function.identity()));
        return materialRepository.findByCourseSubjectIdInOrderByPublishedAtDesc(ids).stream()
                .map(material -> {
                    CourseSubject cs = byId.get(material.getCourseSubjectId());
                    return new TeacherMaterialDto(material.getId(), material.getCourseSubjectId(),
                            cs == null ? "" : lookup.courseName(cs.getCourseId()),
                            cs == null ? "" : lookup.subjectName(cs.getSubjectId()),
                            material.getTitle(), material.getDescription(), material.getType().name(),
                            material.getUrl(), material.getPublishedAt(), material.isVisibleToParents());
                })
                .toList();
    }

    @Transactional
    public TeacherMaterialDto createMaterial(UserPrincipal principal, UUID courseSubjectId,
                                             MaterialCreateRequest request) {
        CourseSubject cs = accessGuard.requireOwnCourseSubject(principal, courseSubjectId);
        Material material = new Material();
        material.setCourseSubjectId(cs.getId());
        material.setTitle(request.title().trim());
        material.setDescription(request.description());
        material.setType(parseMaterialType(request.type()));
        material.setUrl(request.url());
        material.setVisibleToParents(request.visibleToParents());
        material.setCreatedBy(principal.id());
        material.setPublishedAt(Instant.now());
        materialRepository.save(material);

        return new TeacherMaterialDto(material.getId(), cs.getId(), lookup.courseName(cs.getCourseId()),
                lookup.subjectName(cs.getSubjectId()), material.getTitle(), material.getDescription(),
                material.getType().name(), material.getUrl(), material.getPublishedAt(),
                material.isVisibleToParents());
    }

    private void requireStudentInTeachersCourses(TeacherProfile teacher, UUID studentId) {
        List<UUID> courseIds = courseSubjectRepository.findByTeacherId(teacher.getId()).stream()
                .map(CourseSubject::getCourseId).distinct().toList();
        boolean belongs = courseIds.stream()
                .anyMatch(courseId -> enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED)
                        .stream().anyMatch(enrollment -> enrollment.getStudentId().equals(studentId)));
        if (!belongs) {
            throw new AccessDeniedException("El estudiante no pertenece a ninguno de tus cursos");
        }
    }

    private AcademicYear activeYear() {
        return academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new BusinessException("No hay un año académico activo"));
    }

    private AcademicPeriod resolvePeriod(UUID periodId) {
        AcademicYear year = activeYear();
        if (periodId != null) {
            AcademicPeriod period = periodRepository.findById(periodId)
                    .orElseThrow(() -> new NotFoundException("Periodo no encontrado"));
            if (!period.getAcademicYearId().equals(year.getId())) {
                throw new BusinessException("El periodo no pertenece al año académico activo");
            }
            return period;
        }
        List<AcademicPeriod> periods = periodRepository.findByAcademicYearIdOrderBySortOrderAsc(year.getId());
        if (periods.isEmpty()) {
            throw new BusinessException("El año académico no tiene periodos configurados");
        }
        LocalDate today = LocalDate.now();
        return periods.stream()
                .filter(period -> !today.isBefore(period.getStartDate()) && !today.isAfter(period.getEndDate()))
                .findFirst()
                .orElse(periods.get(0));
    }

    private CourseSubjectDto toCourseSubjectDto(CourseSubject cs) {
        return new CourseSubjectDto(cs.getId(), cs.getCourseId(), lookup.courseName(cs.getCourseId()),
                cs.getSubjectId(), lookup.subjectName(cs.getSubjectId()), lookup.teacherName(cs.getTeacherId()),
                cs.getWeeklyHours());
    }

    private PeriodDto toPeriod(AcademicPeriod period) {
        return new PeriodDto(period.getId(), period.getName(), period.getSortOrder(),
                period.getStartDate(), period.getEndDate(), period.getWeight());
    }

    private GradeItemDto toGradeItem(Grade grade) {
        return new GradeItemDto(grade.getId(), grade.getTitle(), grade.getType().name(), grade.getScore(),
                grade.getWeight(), grade.getCreatedAt(), grade.getComment());
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

    private GradeType parseGradeType(String value) {
        try {
            return GradeType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Tipo de calificación inválido: " + value);
        }
    }

    private AttendanceStatus parseAttendanceStatus(String value) {
        try {
            return AttendanceStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Estado de asistencia inválido: " + value);
        }
    }

    private MaterialType parseMaterialType(String value) {
        try {
            return MaterialType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Tipo de material inválido: " + value);
        }
    }

    private ObservationType parseObservationType(String value) {
        try {
            return ObservationType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Tipo de observación inválido: " + value);
        }
    }

    private ObservationVisibility parseObservationVisibility(String value) {
        try {
            return ObservationVisibility.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Visibilidad de observación inválida: " + value);
        }
    }
}
