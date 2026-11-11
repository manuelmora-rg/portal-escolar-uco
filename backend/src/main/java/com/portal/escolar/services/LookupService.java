package com.portal.escolar.services;

import com.portal.escolar.entities.Course;
import com.portal.escolar.entities.GradeLevel;
import com.portal.escolar.entities.StudentProfile;
import com.portal.escolar.entities.TeacherProfile;
import com.portal.escolar.entities.User;
import com.portal.escolar.repositories.CourseRepository;
import com.portal.escolar.repositories.GradeLevelRepository;
import com.portal.escolar.repositories.StudentProfileRepository;
import com.portal.escolar.repositories.SubjectRepository;
import com.portal.escolar.repositories.TeacherProfileRepository;
import com.portal.escolar.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final SubjectRepository subjectRepository;
    private final TeacherProfileRepository teacherRepository;
    private final StudentProfileRepository studentRepository;
    private final CourseRepository courseRepository;
    private final GradeLevelRepository gradeLevelRepository;
    private final UserRepository userRepository;

    public String subjectName(UUID subjectId) {
        return subjectRepository.findById(subjectId).map(s -> s.getName()).orElse("Materia");
    }

    public String teacherName(UUID teacherProfileId) {
        return teacherRepository.findById(teacherProfileId)
                .flatMap(teacher -> userRepository.findById(teacher.getUserId()))
                .map(User::getFullName)
                .orElse("Docente");
    }

    public String studentName(UUID studentId) {
        return studentRepository.findById(studentId)
                .flatMap(student -> userRepository.findById(student.getUserId()))
                .map(User::getFullName)
                .orElse("Estudiante");
    }

    public String studentEmail(UUID studentId) {
        return studentRepository.findById(studentId)
                .flatMap(student -> userRepository.findById(student.getUserId()))
                .map(User::getEmail)
                .orElse("");
    }

    public StudentProfile student(UUID studentId) {
        return studentRepository.findById(studentId).orElse(null);
    }

    public String courseName(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return "Curso";
        }
        String level = gradeLevelRepository.findById(course.getGradeLevelId())
                .map(GradeLevel::getName)
                .orElse("");
        return (level + " " + course.getGroupName()).trim();
    }

    public String courseGradeLevel(UUID courseId) {
        return courseRepository.findById(courseId)
                .flatMap(course -> gradeLevelRepository.findById(course.getGradeLevelId()))
                .map(GradeLevel::getName)
                .orElse("");
    }

    public String courseGroup(UUID courseId) {
        return courseRepository.findById(courseId).map(Course::getGroupName).orElse("");
    }

    public TeacherProfile teacher(UUID teacherProfileId) {
        return teacherRepository.findById(teacherProfileId).orElse(null);
    }

    public User user(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
