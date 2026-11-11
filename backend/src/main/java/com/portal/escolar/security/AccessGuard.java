package com.portal.escolar.security;

import com.portal.escolar.entities.CourseSubject;
import com.portal.escolar.entities.DomainEnums.Role;
import com.portal.escolar.entities.ParentProfile;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.exceptions.NotFoundException;
import com.portal.escolar.repositories.CourseSubjectRepository;
import com.portal.escolar.repositories.ParentProfileRepository;
import com.portal.escolar.repositories.ParentStudentRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de autorización por recurso. Se ejecutan en el backend, nunca solo en el frontend.
 */
@Component
@RequiredArgsConstructor
public class AccessGuard {

    private final StudentProfileRepository studentRepository;
    private final ParentProfileRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final TeacherProfileRepository teacherRepository;
    private final CourseSubjectRepository courseSubjectRepository;

    public StudentProfile requireStudentAccess(UserPrincipal principal, UUID studentId) {
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Estudiante no encontrado"));

        if (principal.role() == Role.ADMIN) {
            return student;
        }
        if (principal.role() == Role.STUDENT) {
            StudentProfile own = requireOwnStudent(principal);
            if (!own.getId().equals(studentId)) {
                throw new AccessDeniedException("No puedes acceder a la información de otro estudiante");
            }
            return student;
        }
        if (principal.role() == Role.PARENT) {
            ParentProfile parent = requireParentProfile(principal);
            if (!parentStudentRepository.existsByParentIdAndStudentIdAndAuthorizedTrue(parent.getId(), studentId)) {
                throw new AccessDeniedException("No tienes autorización sobre este estudiante");
            }
            return student;
        }
        throw new AccessDeniedException("Rol sin acceso a información de estudiantes");
    }

    public StudentProfile requireOwnStudent(UserPrincipal principal) {
        return studentRepository.findByUserId(principal.id())
                .orElseThrow(() -> new NotFoundException("Perfil de estudiante no encontrado"));
    }

    public ParentProfile requireParentProfile(UserPrincipal principal) {
        return parentRepository.findByUserId(principal.id())
                .orElseThrow(() -> new NotFoundException("Perfil de acudiente no encontrado"));
    }

    public TeacherProfile requireTeacherProfile(UserPrincipal principal) {
        return teacherRepository.findByUserId(principal.id())
                .orElseThrow(() -> new NotFoundException("Perfil de docente no encontrado"));
    }

    public CourseSubject requireOwnCourseSubject(UserPrincipal principal, UUID courseSubjectId) {
        CourseSubject courseSubject = courseSubjectRepository.findById(courseSubjectId)
                .orElseThrow(() -> new NotFoundException("Asignación académica no encontrada"));
        if (principal.role() == Role.ADMIN) {
            return courseSubject;
        }
        TeacherProfile teacher = requireTeacherProfile(principal);
        if (!courseSubject.getTeacherId().equals(teacher.getId())) {
            throw new AccessDeniedException("El curso no está asignado a este docente");
        }
        return courseSubject;
    }

    public List<UUID> authorizedChildIds(UserPrincipal principal) {
        ParentProfile parent = requireParentProfile(principal);
        return parentStudentRepository.findByParentIdAndAuthorizedTrue(parent.getId()).stream()
                .map(link -> link.getStudentId())
                .toList();
    }
}
