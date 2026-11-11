package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.CourseDto;
import com.portal.escolar.dto.AcademicDtos.DashboardAdminDto;
import com.portal.escolar.dto.AcademicDtos.NewsSummaryDto;
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
import com.portal.escolar.entities.AcademicYear;
import com.portal.escolar.entities.ContactMessage;
import com.portal.escolar.entities.Course;
import com.portal.escolar.entities.DomainEnums.ContactStatus;
import com.portal.escolar.entities.DomainEnums.NewsStatus;
import com.portal.escolar.entities.DomainEnums.Relationship;
import com.portal.escolar.entities.DomainEnums.Role;
import com.portal.escolar.entities.Enrollment;
import com.portal.escolar.entities.News;
import com.portal.escolar.entities.ParentProfile;
import com.portal.escolar.entities.ParentStudent;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.entities.User;
import com.portal.escolar.exceptions.BusinessException;
import com.portal.escolar.exceptions.ConflictException;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.AcademicYearRepository;
import com.portal.escolar.repositories.ContactMessageRepository;
import com.portal.escolar.repositories.CourseRepository;
import com.portal.escolar.repositories.EnrollmentRepository;
import com.portal.escolar.repositories.NewsRepository;
import com.portal.escolar.repositories.ParentProfileRepository;
import com.portal.escolar.repositories.ParentStudentRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.SubjectRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import com.portal.escolar.repositories.UserRepository;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final ParentProfileRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final NewsRepository newsRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final SettingsService settingsService;
    private final LookupService lookup;
    private final ObjectMapper objectMapper;

    public DashboardAdminDto dashboard() {
        List<NewsSummaryDto> latest = newsRepository.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED).stream()
                .limit(5)
                .map(news -> new NewsSummaryDto(news.getId(), news.getSlug(), news.getTitle(), news.getSummary(),
                        news.getImageUrl(), news.getCategory(), news.getPublishedAt()))
                .toList();

        return new DashboardAdminDto(
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.TEACHER),
                userRepository.countByRole(Role.PARENT),
                courseRepository.count(),
                enrollmentRepository.count(),
                newsRepository.findByStatusOrderByPublishedAtDesc(NewsStatus.PUBLISHED).size(),
                latest
        );
    }

    public List<AdminStudentDto> students() {
        return userRepository.findByRoleOrderByLastNameAscFirstNameAsc(Role.STUDENT).stream()
                .map(user -> {
                    StudentProfile student = studentRepository.findByUserId(user.getId()).orElse(null);
                    Enrollment enrollment = student == null ? null : activeEnrollment(student.getId());
                    return new AdminStudentDto(
                            user.getId(),
                            student == null ? null : student.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            student == null ? "" : student.getDocumentNumber(),
                            enrollment == null ? "" : lookup.courseGradeLevel(enrollment.getCourseId()),
                            enrollment == null ? "" : lookup.courseGroup(enrollment.getCourseId()),
                            student == null ? "" : student.getStatus().name(),
                            user.isEnabled()
                    );
                })
                .toList();
    }

    public List<AdminTeacherDto> teachers() {
        return userRepository.findByRoleOrderByLastNameAscFirstNameAsc(Role.TEACHER).stream()
                .map(user -> {
                    TeacherProfile teacher = teacherRepository.findByUserId(user.getId()).orElse(null);
                    return new AdminTeacherDto(
                            user.getId(),
                            teacher == null ? null : teacher.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            teacher == null ? "" : teacher.getEmployeeCode(),
                            teacher == null ? "" : teacher.getSpecialty(),
                            user.isEnabled()
                    );
                })
                .toList();
    }

    public List<AdminParentDto> parents() {
        return userRepository.findByRoleOrderByLastNameAscFirstNameAsc(Role.PARENT).stream()
                .map(user -> {
                    ParentProfile parent = parentRepository.findByUserId(user.getId()).orElse(null);
                    long children = parent == null ? 0
                            : parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId()).size();
                    return new AdminParentDto(
                            user.getId(),
                            parent == null ? null : parent.getId(),
                            user.getFullName(),
                            user.getEmail(),
                            parent == null ? "" : parent.getDocumentNumber(),
                            parent == null ? "" : parent.getPhone(),
                            children
                    );
                })
                .toList();
    }

    public List<CourseDto> courses() {
        AcademicYear year = academicYearRepository.findByActiveTrue().orElse(null);
        List<Course> courses = year == null ? courseRepository.findAll()
                : courseRepository.findByAcademicYearId(year.getId());
        return courses.stream()
                .map(course -> new CourseDto(
                        course.getId(),
                        lookup.courseName(course.getId()),
                        lookup.courseGradeLevel(course.getId()),
                        course.getGroupName(),
                        enrollmentRepository.countByCourseIdAndStatus(course.getId(),
                                com.portal.escolar.entities.DomainEnums.EnrollmentStatus.ENROLLED),
                        course.getHomeroomTeacherId()
                ))
                .sorted(Comparator.comparing(CourseDto::name))
                .toList();
    }

    public List<SubjectDto> subjects() {
        return subjectRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(subject -> new SubjectDto(subject.getId(), subject.getCode(), subject.getName(), subject.getArea()))
                .toList();
    }

    public List<AdminEnrollmentDto> enrollments() {
        AcademicYear year = academicYearRepository.findByActiveTrue().orElse(null);
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(enrollment -> year == null || enrollment.getAcademicYearId().equals(year.getId()))
                .toList();
        return enrollments.stream()
                .map(enrollment -> new AdminEnrollmentDto(
                        enrollment.getId(),
                        lookup.studentName(enrollment.getStudentId()),
                        lookup.courseName(enrollment.getCourseId()),
                        academicYearRepository.findById(enrollment.getAcademicYearId())
                                .map(AcademicYear::getName).orElse(""),
                        enrollment.getStatus().name(),
                        enrollment.getEnrollmentDate()
                ))
                .sorted(Comparator.comparing(AdminEnrollmentDto::courseName)
                        .thenComparing(AdminEnrollmentDto::studentName))
                .toList();
    }

    public List<AdminNewsDto> news() {
        return newsRepository.findAll().stream()
                .sorted(Comparator.comparing(News::getCreatedAt).reversed())
                .map(this::toNewsDto)
                .toList();
    }

    @Transactional
    public AdminNewsDto createNews(UserPrincipal principal, NewsRequest request) {
        News news = new News();
        news.setSlug(uniqueSlug(request.title(), null));
        applyNews(news, request, principal);
        newsRepository.save(news);
        return toNewsDto(news);
    }

    @Transactional
    public AdminNewsDto updateNews(UserPrincipal principal, UUID newsId, NewsRequest request) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));
        if (!news.getTitle().equalsIgnoreCase(request.title())) {
            news.setSlug(uniqueSlug(request.title(), news.getId()));
        }
        applyNews(news, request, principal);
        newsRepository.save(news);
        return toNewsDto(news);
    }

    @Transactional
    public AdminNewsDto publishNews(UUID newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new NotFoundException("Noticia no encontrada"));
        news.setStatus(NewsStatus.PUBLISHED);
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(Instant.now());
        }
        newsRepository.save(news);
        return toNewsDto(news);
    }

    public List<ContactMessageDto> contactMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toContactDto)
                .toList();
    }

    @Transactional
    public ContactMessageDto updateContactStatus(UUID messageId, ContactStatusRequest request) {
        ContactMessage message = contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException("Mensaje no encontrado"));
        try {
            message.setStatus(ContactStatus.valueOf(request.status().trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Estado de mensaje inválido: " + request.status());
        }
        contactMessageRepository.save(message);
        return toContactDto(message);
    }

    public PublicInstitutionDto institution() {
        return settingsService.institution();
    }

    @Transactional
    public PublicInstitutionDto updateInstitution(Map<String, Object> values) {
        try {
            settingsService.updateInstitution(objectMapper.writeValueAsString(values));
        } catch (Exception ex) {
            throw new BusinessException("No fue posible actualizar la configuración institucional");
        }
        return settingsService.institution();
    }

    @Transactional
    public void linkParent(ParentLinkRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.parentEmail())
                .orElseThrow(() -> new NotFoundException("Acudiente no encontrado"));
        if (user.getRole() != Role.PARENT) {
            throw new BusinessException("El usuario indicado no tiene rol de acudiente");
        }
        ParentProfile parent = parentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Perfil de acudiente no encontrado"));
        StudentProfile student = studentRepository.findByDocumentNumber(request.studentDocument())
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));

        Relationship relationship;
        try {
            relationship = Relationship.valueOf(request.relationship().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Parentesco inválido: " + request.relationship());
        }

        ParentStudent link = parentStudentRepository
                .findByParentIdAndStudentId(parent.getId(), student.getId())
                .orElse(null);
        if (link != null && link.isAuthorized()) {
            throw new ConflictException("El vínculo familiar ya existe y está autorizado");
        }
        if (link == null) {
            link = new ParentStudent();
            link.setParentId(parent.getId());
            link.setStudentId(student.getId());
        }
        link.setRelationship(relationship);
        link.setAuthorized(true);
        parentStudentRepository.save(link);
    }

    private void applyNews(News news, NewsRequest request, UserPrincipal principal) {
        news.setTitle(request.title().trim());
        news.setSummary(request.summary());
        news.setContent(request.content());
        news.setImageUrl(request.imageUrl());
        news.setCategory(request.category());
        news.setAuthorId(principal.id());
        if (request.publish()) {
            news.setStatus(NewsStatus.PUBLISHED);
            if (news.getPublishedAt() == null) {
                news.setPublishedAt(Instant.now());
            }
        } else if (news.getStatus() == null) {
            news.setStatus(NewsStatus.DRAFT);
        }
    }

    private String uniqueSlug(String title, UUID currentId) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "noticia";
        }
        String candidate = base;
        int suffix = 2;
        while (true) {
            String slug = candidate;
            boolean taken = newsRepository.findAll().stream()
                    .anyMatch(news -> news.getSlug().equals(slug)
                            && (currentId == null || !news.getId().equals(currentId)));
            if (!taken) {
                return candidate;
            }
            candidate = base + "-" + suffix++;
        }
    }

    private Enrollment activeEnrollment(UUID studentId) {
        AcademicYear year = academicYearRepository.findByActiveTrue().orElse(null);
        if (year == null) {
            return null;
        }
        return enrollmentRepository.findByStudentIdAndAcademicYearId(studentId, year.getId()).orElse(null);
    }

    private AdminNewsDto toNewsDto(News news) {
        String author = null;
        if (news.getAuthorId() != null) {
            User user = lookup.user(news.getAuthorId());
            author = user == null ? null : user.getFullName();
        }
        return new AdminNewsDto(news.getId(), news.getSlug(), news.getTitle(), news.getSummary(), news.getContent(),
                news.getImageUrl(), news.getCategory(), news.getStatus().name(), news.getPublishedAt(), author);
    }

    private ContactMessageDto toContactDto(ContactMessage message) {
        return new ContactMessageDto(message.getId(), message.getFullName(), message.getEmail(), message.getPhone(),
                message.getSubject(), message.getMessage(), message.getStatus().name(), message.getCreatedAt());
    }
}
