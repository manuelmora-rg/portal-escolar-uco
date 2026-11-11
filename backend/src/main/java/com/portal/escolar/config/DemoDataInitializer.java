package com.portal.escolar.config;

import com.portal.escolar.entities.AcademicPeriod;
import com.portal.escolar.entities.AcademicYear;
import com.portal.escolar.entities.Assignment;
import com.portal.escolar.entities.AttendanceRecord;
import com.portal.escolar.entities.Course;
import com.portal.escolar.entities.CourseSubject;
import com.portal.escolar.entities.DomainEnums.AssignmentStatus;
import com.portal.escolar.entities.DomainEnums.AttendanceStatus;
import com.portal.escolar.entities.DomainEnums.EnrollmentStatus;
import com.portal.escolar.entities.DomainEnums.GradeType;
import com.portal.escolar.entities.DomainEnums.NotificationType;
import com.portal.escolar.entities.DomainEnums.ObservationType;
import com.portal.escolar.entities.DomainEnums.ObservationVisibility;
import com.portal.escolar.entities.DomainEnums.Relationship;
import com.portal.escolar.entities.DomainEnums.Role;
import com.portal.escolar.entities.DomainEnums.SubmissionStatus;
import com.portal.escolar.entities.Enrollment;
import com.portal.escolar.entities.Grade;
import com.portal.escolar.entities.GradeLevel;
import com.portal.escolar.entities.Meeting;
import com.portal.escolar.entities.MeetingAttendance;
import com.portal.escolar.entities.Notification;
import com.portal.escolar.entities.Observation;
import com.portal.escolar.entities.ParentProfile;
import com.portal.escolar.entities.ParentStudent;
import com.portal.escolar.entities.ScheduleBlock;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.Subject;
import com.portal.escolar.entities.Submission;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.entities.User;
import com.portal.escolar.repositories.AcademicPeriodRepository;
import com.portal.escolar.repositories.AcademicYearRepository;
import com.portal.escolar.repositories.AssignmentRepository;
import com.portal.escolar.repositories.AttendanceRecordRepository;
import com.portal.escolar.repositories.CourseRepository;
import com.portal.escolar.repositories.CourseSubjectRepository;
import com.portal.escolar.repositories.EnrollmentRepository;
import com.portal.escolar.repositories.GradeLevelRepository;
import com.portal.escolar.repositories.GradeRepository;
import com.portal.escolar.repositories.MeetingAttendanceRepository;
import com.portal.escolar.repositories.MeetingRepository;
import com.portal.escolar.repositories.NotificationRepository;
import com.portal.escolar.repositories.ObservationRepository;
import com.portal.escolar.repositories.ParentProfileRepository;
import com.portal.escolar.repositories.ParentStudentRepository;
import com.portal.escolar.repositories.ScheduleBlockRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.SubjectRepository;
import com.portal.escolar.repositories.SubmissionRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import com.portal.escolar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Datos ficticios para desarrollo. No se ejecuta en producción.
 * Las contraseñas se generan con BCrypt en tiempo de ejecución; nunca se guardan en texto plano.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final ParentProfileRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicPeriodRepository periodRepository;
    private final GradeLevelRepository gradeLevelRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final CourseSubjectRepository courseSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScheduleBlockRepository scheduleRepository;
    private final GradeRepository gradeRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final ObservationRepository observationRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository meetingAttendanceRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.password}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Los datos de demostración ya existen; no se vuelven a crear.");
            return;
        }

        String hash = passwordEncoder.encode(demoPassword);
        Random random = new Random(2026);

        User admin = createUser("admin@portalescolar.edu.co", "Ana", "Martínez", Role.ADMIN, hash);
        User teacherUser1 = createUser("laura.gomez@portalescolar.edu.co", "Laura", "Gómez", Role.TEACHER, hash);
        User teacherUser2 = createUser("carlos.ruiz@portalescolar.edu.co", "Carlos", "Ruiz", Role.TEACHER, hash);

        TeacherProfile laura = createTeacher(teacherUser1, "DOC-001", "Matemáticas y Ciencias");
        TeacherProfile carlos = createTeacher(teacherUser2, "DOC-002", "Lenguaje y Humanidades");

        List<Course> courses = courseRepository.findAll();
        Map<UUID, GradeLevel> gradeLevels = new HashMap<>();
        gradeLevelRepository.findAll().forEach(level -> gradeLevels.put(level.getId(), level));

        courses.forEach(course -> {
            boolean sixth = gradeLevels.get(course.getGradeLevelId()).getSortOrder() == 6;
            course.setHomeroomTeacherId(sixth ? laura.getId() : carlos.getId());
            courseRepository.save(course);
        });

        List<Subject> subjects = subjectRepository.findAll();
        List<CourseSubject> courseSubjects = new ArrayList<>();
        for (Course course : courses) {
            int index = 0;
            for (Subject subject : subjects) {
                CourseSubject cs = new CourseSubject();
                cs.setCourseId(course.getId());
                cs.setSubjectId(subject.getId());
                cs.setTeacherId(index % 2 == 0 ? laura.getId() : carlos.getId());
                cs.setWeeklyHours(4);
                courseSubjects.add(courseSubjectRepository.save(cs));
                index++;
            }
        }

        createSchedule(courseSubjects);

        AcademicYear year = academicYearRepository.findByActiveTrue().orElseThrow();
        List<AcademicPeriod> periods = periodRepository.findByAcademicYearIdOrderBySortOrderAsc(year.getId());

        record StudentSeed(String firstName, String lastName, String document, String email, int grade, String group) {
        }
        List<StudentSeed> seeds = List.of(
                new StudentSeed("Sofía", "Mora Giraldo", "TI-1001", "sofia.mora@portalescolar.edu.co", 6, "A"),
                new StudentSeed("Juan", "Pérez Salazar", "TI-1002", "juan.perez@portalescolar.edu.co", 6, "A"),
                new StudentSeed("Valentina", "García Ríos", "TI-1003", "valentina.garcia@portalescolar.edu.co", 6, "A"),
                new StudentSeed("Santiago", "López Mejía", "TI-1004", "santiago.lopez@portalescolar.edu.co", 6, "B"),
                new StudentSeed("Isabella", "Torres Cano", "TI-1005", "isabella.torres@portalescolar.edu.co", 10, "A"),
                new StudentSeed("Mateo", "Ramírez Ospina", "TI-1006", "mateo.ramirez@portalescolar.edu.co", 11, "A")
        );

        Map<String, StudentProfile> studentsByEmail = new HashMap<>();
        Map<String, Course> courseByKey = new HashMap<>();
        for (Course course : courses) {
            GradeLevel level = gradeLevels.get(course.getGradeLevelId());
            courseByKey.put(level.getSortOrder() + "-" + course.getGroupName(), course);
        }

        for (StudentSeed seed : seeds) {
            User user = createUser(seed.email(), seed.firstName(), seed.lastName(), Role.STUDENT, hash);
            StudentProfile student = new StudentProfile();
            student.setUserId(user.getId());
            student.setDocumentType("TI");
            student.setDocumentNumber(seed.document());
            student.setBirthDate(LocalDate.of(2026 - (seed.grade() + 5), 4, 12));
            student.setGender(seed.firstName().endsWith("a") ? "F" : "M");
            student.setAddress("Dirección ficticia de demostración");
            student.setAdmissionDate(LocalDate.of(2026, 2, 2));
            student = studentRepository.save(student);
            studentsByEmail.put(seed.email(), student);

            Course course = courseByKey.get(seed.grade() + "-" + seed.group());
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(student.getId());
            enrollment.setCourseId(course.getId());
            enrollment.setAcademicYearId(year.getId());
            enrollment.setEnrollmentDate(LocalDate.of(2026, 2, 2));
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollmentRepository.save(enrollment);
        }

        // Acudientes y vínculos autorizados
        record ParentSeed(String firstName, String lastName, String email, String document, String childEmail, Relationship relationship) {
        }
        List<ParentSeed> parentSeeds = List.of(
                new ParentSeed("María", "Giraldo", "maria.giraldo@portalescolar.edu.co", "CC-2001", "sofia.mora@portalescolar.edu.co", Relationship.MOTHER),
                new ParentSeed("Andrés", "Pérez", "andres.perez@portalescolar.edu.co", "CC-2002", "juan.perez@portalescolar.edu.co", Relationship.FATHER),
                new ParentSeed("Claudia", "Ríos", "claudia.rios@portalescolar.edu.co", "CC-2003", "valentina.garcia@portalescolar.edu.co", Relationship.MOTHER),
                new ParentSeed("Jorge", "López", "jorge.lopez@portalescolar.edu.co", "CC-2004", "santiago.lopez@portalescolar.edu.co", Relationship.FATHER)
        );

        List<ParentProfile> parents = new ArrayList<>();
        for (ParentSeed seed : parentSeeds) {
            User user = createUser(seed.email(), seed.firstName(), seed.lastName(), Role.PARENT, hash);
            ParentProfile parent = new ParentProfile();
            parent.setUserId(user.getId());
            parent.setDocumentNumber(seed.document());
            parent.setPhone("+57 300 000 0000");
            parent.setOccupation("Ocupación ficticia");
            parent.setAddress("Dirección ficticia de demostración");
            parent = parentRepository.save(parent);
            parents.add(parent);

            ParentStudent link = new ParentStudent();
            link.setParentId(parent.getId());
            link.setStudentId(studentsByEmail.get(seed.childEmail()).getId());
            link.setRelationship(seed.relationship());
            link.setAuthorized(true);
            parentStudentRepository.save(link);
        }

        // Calificaciones por estudiante, materia y periodo
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        List<AcademicPeriod> usedPeriods = periods.subList(0, Math.min(2, periods.size()));
        for (Enrollment enrollment : enrollments) {
            List<CourseSubject> ofCourse = courseSubjects.stream()
                    .filter(cs -> cs.getCourseId().equals(enrollment.getCourseId()))
                    .toList();
            for (CourseSubject cs : ofCourse) {
                int idx = 0;
                for (AcademicPeriod period : usedPeriods) {
                    int gradesInPeriod = period.getSortOrder() == 1 ? 2 : 1;
                    for (int g = 0; g < gradesInPeriod; g++) {
                        Grade grade = new Grade();
                        grade.setEnrollmentId(enrollment.getId());
                        grade.setCourseSubjectId(cs.getId());
                        grade.setPeriodId(period.getId());
                        grade.setTitle(g == 0 ? "Evaluación acumulativa" : (g == 1 ? "Taller en clase" : "Quiz de refuerzo"));
                        grade.setType(g == 0 ? GradeType.EXAM : (g == 1 ? GradeType.HOMEWORK : GradeType.QUIZ));
                        BigDecimal score = BigDecimal.valueOf(3.2 + random.nextDouble() * 1.8)
                                .setScale(1, RoundingMode.HALF_UP);
                        grade.setScore(score.min(BigDecimal.valueOf(5)));
                        grade.setWeight(g == 0 ? BigDecimal.valueOf(40) : BigDecimal.valueOf(30));
                        grade.setGradedBy(cs.getTeacherId().equals(laura.getId()) ? teacherUser1.getId() : teacherUser2.getId());
                        grade.setComment("Registro de demostración");
                        gradeRepository.save(grade);
                    }
                }
                idx++;
            }
        }

        // Tareas y entregas
        for (CourseSubject cs : courseSubjects) {
            Assignment assignment = new Assignment();
            assignment.setCourseSubjectId(cs.getId());
            assignment.setTitle("Actividad de refuerzo");
            assignment.setDescription("Desarrolla la actividad propuesta y súbela antes de la fecha límite. Datos de demostración.");
            assignment.setDueAt(Instant.now().plus(5, ChronoUnit.DAYS));
            assignment.setMaxScore(BigDecimal.valueOf(5));
            assignment.setStatus(AssignmentStatus.PUBLISHED);
            assignment.setCreatedBy(cs.getTeacherId().equals(laura.getId()) ? teacherUser1.getId() : teacherUser2.getId());
            assignment = assignmentRepository.save(assignment);

            List<Enrollment> courseEnrollments = enrollments.stream()
                    .filter(e -> e.getCourseId().equals(cs.getCourseId()))
                    .toList();
            for (int i = 0; i < courseEnrollments.size(); i++) {
                if (i % 2 == 0) {
                    Submission submission = new Submission();
                    submission.setAssignmentId(assignment.getId());
                    submission.setStudentId(courseEnrollments.get(i).getStudentId());
                    submission.setSubmittedAt(Instant.now().minus(1, ChronoUnit.DAYS));
                    submission.setComment("Entrega de demostración");
                    submission.setStatus(SubmissionStatus.SUBMITTED);
                    submissionRepository.save(submission);
                }
            }
        }

        // Asistencia de los últimos 10 días hábiles
        LocalDate today = LocalDate.now();
        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = today.minusDays(1);
        while (days.size() < 10) {
            if (cursor.getDayOfWeek() != DayOfWeek.SATURDAY && cursor.getDayOfWeek() != DayOfWeek.SUNDAY) {
                days.add(cursor);
            }
            cursor = cursor.minusDays(1);
        }
        AttendanceStatus[] statuses = {AttendanceStatus.PRESENT, AttendanceStatus.PRESENT, AttendanceStatus.PRESENT,
                AttendanceStatus.LATE, AttendanceStatus.PRESENT, AttendanceStatus.ABSENT};
        for (Enrollment enrollment : enrollments) {
            for (int i = 0; i < days.size(); i++) {
                AttendanceRecord record = new AttendanceRecord();
                record.setEnrollmentId(enrollment.getId());
                record.setAttendanceDate(days.get(i));
                record.setStatus(statuses[random.nextInt(statuses.length)]);
                record.setRecordedBy(teacherUser1.getId());
                attendanceRepository.save(record);
            }
        }

        // Observaciones
        StudentProfile sofia = studentsByEmail.get("sofia.mora@portalescolar.edu.co");
        StudentProfile juan = studentsByEmail.get("juan.perez@portalescolar.edu.co");
        StudentProfile isabella = studentsByEmail.get("isabella.torres@portalescolar.edu.co");
        createObservation(sofia.getId(), laura.getId(), ObservationType.PRAISE,
                "Sofía demuestra liderazgo y participación constante en clase.", ObservationVisibility.PARENT);
        createObservation(juan.getId(), carlos.getId(), ObservationType.ACADEMIC,
                "Se recomienda reforzar hábitos de estudio y entrega puntual de trabajos.", ObservationVisibility.PARENT);
        createObservation(isabella.getId(), laura.getId(), ObservationType.FOLLOW_UP,
                "Seguimiento interno de orientación escolar.", ObservationVisibility.INTERNAL);

        // Reuniones y confirmaciones
        for (Course course : courses) {
            GradeLevel level = gradeLevels.get(course.getGradeLevelId());
            Meeting meeting = new Meeting();
            meeting.setTitle("Reunión de padres - " + level.getName() + " " + course.getGroupName());
            meeting.setDescription("Encuentro para revisar avances académicos y acuerdos del periodo.");
            meeting.setScheduledAt(Instant.now().plus(7, ChronoUnit.DAYS));
            meeting.setCourseId(course.getId());
            meeting.setTeacherId(course.getHomeroomTeacherId());
            meeting.setLocation("Auditorio principal");
            meeting.setStatus("SCHEDULED");
            meeting = meetingRepository.save(meeting);

            List<Enrollment> courseEnrollments = enrollments.stream()
                    .filter(e -> e.getCourseId().equals(course.getId()))
                    .toList();
            List<UUID> studentIds = courseEnrollments.stream().map(Enrollment::getStudentId).toList();
            for (ParentProfile parent : parents) {
                List<ParentStudent> links = parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId());
                boolean hasChildInCourse = links.stream().anyMatch(link -> studentIds.contains(link.getStudentId()));
                if (hasChildInCourse) {
                    MeetingAttendance attendance = new MeetingAttendance();
                    attendance.setMeetingId(meeting.getId());
                    attendance.setParentId(parent.getId());
                    meetingAttendanceRepository.save(attendance);
                }
            }
        }

        // Notificaciones
        createNotification(admin.getId(), "Panel administrativo listo",
                "La plataforma está configurada con datos de demostración.", NotificationType.SYSTEM, "/app/admin");
        createNotification(teacherUser1.getId(), "Entregas pendientes",
                "Hay trabajos de tus cursos pendientes por revisar.", NotificationType.ASSIGNMENT, "/app/docente/tareas");
        createNotification(teacherUser2.getId(), "Reunión programada",
                "Tienes una reunión de padres en los próximos días.", NotificationType.MEETING, "/app/docente");
        for (StudentSeed seed : seeds) {
            User studentUser = userRepository.findByEmailIgnoreCase(seed.email()).orElseThrow();
            createNotification(studentUser.getId(), "Nueva calificación registrada",
                    "Se registraron nuevas notas en tu boletín.", NotificationType.GRADE, "/app/estudiante/calificaciones");
        }
        for (ParentSeed seed : parentSeeds) {
            User parentUser = userRepository.findByEmailIgnoreCase(seed.email()).orElseThrow();
            createNotification(parentUser.getId(), "Seguimiento académico disponible",
                    "Ya puedes consultar las notas y la asistencia de tu hijo(a).", NotificationType.INFO, "/app/padre");
        }

        log.info("Datos de demostración creados. Usuario admin: admin@portalescolar.edu.co / {}", demoPassword);
    }

    private User createUser(String email, String firstName, String lastName, Role role, String hash) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setPasswordHash(hash);
        user.setEnabled(true);
        user.setMustChangePassword(false);
        return userRepository.save(user);
    }

    private TeacherProfile createTeacher(User user, String code, String specialty) {
        TeacherProfile teacher = new TeacherProfile();
        teacher.setUserId(user.getId());
        teacher.setEmployeeCode(code);
        teacher.setSpecialty(specialty);
        teacher.setHireDate(LocalDate.of(2020, 1, 20));
        teacher.setPhone("+57 300 000 0001");
        return teacherRepository.save(teacher);
    }

    private void createSchedule(List<CourseSubject> courseSubjects) {
        LocalTime[] starts = {LocalTime.of(7, 0), LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0)};
        int index = 0;
        for (CourseSubject cs : courseSubjects) {
            for (int block = 0; block < 2; block++) {
                int day = (index + block) % 5 + 1;
                LocalTime start = starts[(index + block) % starts.length];
                ScheduleBlock schedule = new ScheduleBlock();
                schedule.setCourseSubjectId(cs.getId());
                schedule.setDayOfWeek((short) day);
                schedule.setStartTime(start);
                schedule.setEndTime(start.plusHours(1));
                schedule.setClassroom("Aula " + (101 + index % 8));
                scheduleRepository.save(schedule);
            }
            index++;
        }
    }

    private void createObservation(UUID studentId, UUID teacherId, ObservationType type, String description,
                                   ObservationVisibility visibility) {
        Observation observation = new Observation();
        observation.setStudentId(studentId);
        observation.setTeacherId(teacherId);
        observation.setType(type);
        observation.setDescription(description);
        observation.setVisibility(visibility);
        observationRepository.save(observation);
    }

    private void createNotification(UUID userId, String title, String message, NotificationType type, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLink(link);
        notificationRepository.save(notification);
    }
}
