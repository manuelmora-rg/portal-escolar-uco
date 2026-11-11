package com.portal.escolar.controllers;

import com.portal.escolar.dto.AcademicDtos.CourseSubjectDto;
import com.portal.escolar.dto.AcademicDtos.DashboardTeacherDto;
import com.portal.escolar.dto.AcademicDtos.GradeItemDto;
import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.TeacherDtos.AssignmentCreateRequest;
import com.portal.escolar.dto.TeacherDtos.AttendanceBatchRequest;
import com.portal.escolar.dto.TeacherDtos.AttendanceSheetDto;
import com.portal.escolar.dto.TeacherDtos.GradeBatchRequest;
import com.portal.escolar.dto.TeacherDtos.GradeEntry;
import com.portal.escolar.dto.TeacherDtos.GradebookResponse;
import com.portal.escolar.dto.TeacherDtos.MaterialCreateRequest;
import com.portal.escolar.dto.TeacherDtos.ObservationCreateRequest;
import com.portal.escolar.dto.TeacherDtos.SubmissionDto;
import com.portal.escolar.dto.TeacherDtos.SubmissionGradeRequest;
import com.portal.escolar.dto.TeacherDtos.TeacherAssignmentDto;
import com.portal.escolar.dto.TeacherDtos.TeacherCourseDto;
import com.portal.escolar.dto.TeacherDtos.TeacherMaterialDto;
import com.portal.escolar.dto.TeacherDtos.TeacherObservationDto;
import com.portal.escolar.dto.AcademicDtos.TeacherStudentDto;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.services.NotificationService;
import com.portal.escolar.services.TeacherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teacher")
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardTeacherDto> dashboard() {
        return ResponseEntity.ok(teacherService.dashboard(CurrentUser.require()));
    }

    @GetMapping("/periods")
    public ResponseEntity<List<PeriodDto>> periods() {
        return ResponseEntity.ok(teacherService.periods());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<TeacherCourseDto>> courses() {
        return ResponseEntity.ok(teacherService.courses(CurrentUser.require()));
    }

    @GetMapping("/course-subjects")
    public ResponseEntity<List<CourseSubjectDto>> courseSubjects() {
        return ResponseEntity.ok(teacherService.courseSubjects(CurrentUser.require()));
    }

    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<TeacherStudentDto>> students(@PathVariable UUID courseId) {
        return ResponseEntity.ok(teacherService.students(CurrentUser.require(), courseId));
    }

    @GetMapping("/course-subjects/{courseSubjectId}/gradebook")
    public ResponseEntity<GradebookResponse> gradebook(@PathVariable UUID courseSubjectId,
                                                       @RequestParam(required = false) UUID periodId) {
        return ResponseEntity.ok(teacherService.gradebook(CurrentUser.require(), courseSubjectId, periodId));
    }

    @PostMapping("/course-subjects/{courseSubjectId}/grades")
    public ResponseEntity<GradebookResponse> saveGrades(@PathVariable UUID courseSubjectId,
                                                        @Valid @RequestBody GradeBatchRequest request) {
        return ResponseEntity.ok(teacherService.saveGrades(CurrentUser.require(), courseSubjectId, request));
    }

    @PutMapping("/grades/{gradeId}")
    public ResponseEntity<GradeItemDto> updateGrade(@PathVariable UUID gradeId,
                                                    @Valid @RequestBody GradeEntry entry,
                                                    HttpServletRequest request) {
        return ResponseEntity.ok(teacherService.updateGrade(CurrentUser.require(), gradeId, entry, clientIp(request)));
    }

    @GetMapping("/course-subjects/{courseSubjectId}/attendance")
    public ResponseEntity<AttendanceSheetDto> attendance(
            @PathVariable UUID courseSubjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(teacherService.attendanceSheet(CurrentUser.require(), courseSubjectId, date));
    }

    @PostMapping("/course-subjects/{courseSubjectId}/attendance")
    public ResponseEntity<AttendanceSheetDto> saveAttendance(@PathVariable UUID courseSubjectId,
                                                             @Valid @RequestBody AttendanceBatchRequest request) {
        return ResponseEntity.ok(teacherService.saveAttendance(CurrentUser.require(), courseSubjectId, request));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<TeacherAssignmentDto>> assignments() {
        return ResponseEntity.ok(teacherService.assignments(CurrentUser.require()));
    }

    @PostMapping("/course-subjects/{courseSubjectId}/assignments")
    public ResponseEntity<TeacherAssignmentDto> createAssignment(@PathVariable UUID courseSubjectId,
                                                                 @Valid @RequestBody AssignmentCreateRequest request) {
        return ResponseEntity.ok(teacherService.createAssignment(CurrentUser.require(), courseSubjectId, request));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionDto>> submissions(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(teacherService.submissions(CurrentUser.require(), assignmentId));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<SubmissionDto> gradeSubmission(@PathVariable UUID submissionId,
                                                         @Valid @RequestBody SubmissionGradeRequest request) {
        return ResponseEntity.ok(teacherService.gradeSubmission(CurrentUser.require(), submissionId, request));
    }

    @GetMapping("/observations")
    public ResponseEntity<List<TeacherObservationDto>> observations() {
        return ResponseEntity.ok(teacherService.observations(CurrentUser.require()));
    }

    @PostMapping("/observations")
    public ResponseEntity<TeacherObservationDto> createObservation(@Valid @RequestBody ObservationCreateRequest request) {
        return ResponseEntity.ok(teacherService.createObservation(CurrentUser.require(), request));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<TeacherMaterialDto>> materials() {
        return ResponseEntity.ok(teacherService.materials(CurrentUser.require()));
    }

    @PostMapping("/course-subjects/{courseSubjectId}/materials")
    public ResponseEntity<TeacherMaterialDto> createMaterial(@PathVariable UUID courseSubjectId,
                                                             @Valid @RequestBody MaterialCreateRequest request) {
        return ResponseEntity.ok(teacherService.createMaterial(CurrentUser.require(), courseSubjectId, request));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications() {
        return ResponseEntity.ok(notificationService.list(CurrentUser.require()));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable UUID notificationId) {
        notificationService.markRead(CurrentUser.require(), notificationId);
        return ResponseEntity.ok(Map.of("message", "Notificación marcada como leída"));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
