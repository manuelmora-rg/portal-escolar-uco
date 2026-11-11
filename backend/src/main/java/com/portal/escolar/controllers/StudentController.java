package com.portal.escolar.controllers;

import com.portal.escolar.dto.AcademicDtos.AssignmentDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceResponse;
import com.portal.escolar.dto.AcademicDtos.DashboardStudentDto;
import com.portal.escolar.dto.AcademicDtos.MaterialDto;
import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.dto.AcademicDtos.ObservationDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.AcademicDtos.ScheduleItemDto;
import com.portal.escolar.dto.AcademicDtos.StudentSummaryDto;
import com.portal.escolar.dto.AcademicDtos.SubjectGradesDto;
import com.portal.escolar.dto.AcademicDtos.SubmissionDto;
import com.portal.escolar.dto.StudentDtos.SubmissionRequest;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.security.UserPrincipal;
import com.portal.escolar.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/profile")
    public ResponseEntity<StudentSummaryDto> profile() {
        UserPrincipal principal = CurrentUser.require();
        StudentService.StudentContext ctx = studentService.context(principal);
        return ResponseEntity.ok(studentService.summary(ctx.student(), ctx.enrollment()));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStudentDto> dashboard() {
        return ResponseEntity.ok(studentService.dashboard(CurrentUser.require()));
    }

    @GetMapping("/periods")
    public ResponseEntity<List<PeriodDto>> periods() {
        return ResponseEntity.ok(studentService.periods());
    }

    @GetMapping("/grades")
    public ResponseEntity<List<SubjectGradesDto>> grades(@RequestParam(required = false) UUID periodId) {
        return ResponseEntity.ok(studentService.grades(CurrentUser.require(), periodId));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<ScheduleItemDto>> schedule() {
        return ResponseEntity.ok(studentService.schedule(CurrentUser.require()));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDto>> assignments() {
        return ResponseEntity.ok(studentService.assignments(CurrentUser.require()));
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<SubmissionDto> submit(@PathVariable UUID assignmentId,
                                                @Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.ok(studentService.submit(CurrentUser.require(), assignmentId, request));
    }

    @GetMapping("/materials")
    public ResponseEntity<List<MaterialDto>> materials() {
        return ResponseEntity.ok(studentService.materials(CurrentUser.require()));
    }

    @GetMapping("/attendance")
    public ResponseEntity<AttendanceResponse> attendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(studentService.attendance(CurrentUser.require(), from, to));
    }

    @GetMapping("/observations")
    public ResponseEntity<List<ObservationDto>> observations() {
        return ResponseEntity.ok(studentService.observations(CurrentUser.require()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> notifications() {
        return ResponseEntity.ok(studentService.notifications(CurrentUser.require()));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable UUID notificationId) {
        studentService.markNotificationRead(CurrentUser.require(), notificationId);
        return ResponseEntity.ok(Map.of("message", "Notificación marcada como leída"));
    }
}
