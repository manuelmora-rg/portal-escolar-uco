package com.portal.escolar.controllers;

import com.portal.escolar.dto.AcademicDtos.AssignmentDto;
import com.portal.escolar.dto.AcademicDtos.AttendanceResponse;
import com.portal.escolar.dto.AcademicDtos.ChildDto;
import com.portal.escolar.dto.AcademicDtos.DashboardParentDto;
import com.portal.escolar.dto.AcademicDtos.MeetingDto;
import com.portal.escolar.dto.AcademicDtos.NotificationDto;
import com.portal.escolar.dto.AcademicDtos.ObservationDto;
import com.portal.escolar.dto.AcademicDtos.PeriodDto;
import com.portal.escolar.dto.AcademicDtos.ScheduleItemDto;
import com.portal.escolar.dto.AcademicDtos.SubjectGradesDto;
import com.portal.escolar.dto.ParentDtos.ChildOverviewDto;
import com.portal.escolar.dto.ParentDtos.MeetingConfirmRequest;
import com.portal.escolar.security.CurrentUser;
import com.portal.escolar.services.NotificationService;
import com.portal.escolar.services.ParentService;
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
@RequestMapping("/api/v1/parent")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardParentDto> dashboard() {
        return ResponseEntity.ok(parentService.dashboard(CurrentUser.require()));
    }

    @GetMapping("/periods")
    public ResponseEntity<List<PeriodDto>> periods() {
        return ResponseEntity.ok(parentService.periods());
    }

    @GetMapping("/children")
    public ResponseEntity<List<ChildDto>> children() {
        return ResponseEntity.ok(parentService.children(CurrentUser.require()));
    }

    @GetMapping("/children/{studentId}/overview")
    public ResponseEntity<ChildOverviewDto> overview(@PathVariable UUID studentId) {
        return ResponseEntity.ok(parentService.overview(CurrentUser.require(), studentId));
    }

    @GetMapping("/children/{studentId}/grades")
    public ResponseEntity<List<SubjectGradesDto>> grades(@PathVariable UUID studentId,
                                                         @RequestParam(required = false) UUID periodId) {
        return ResponseEntity.ok(parentService.grades(CurrentUser.require(), studentId, periodId));
    }

    @GetMapping("/children/{studentId}/schedule")
    public ResponseEntity<List<ScheduleItemDto>> schedule(@PathVariable UUID studentId) {
        return ResponseEntity.ok(parentService.schedule(CurrentUser.require(), studentId));
    }

    @GetMapping("/children/{studentId}/assignments")
    public ResponseEntity<List<AssignmentDto>> assignments(@PathVariable UUID studentId) {
        return ResponseEntity.ok(parentService.assignments(CurrentUser.require(), studentId));
    }

    @GetMapping("/children/{studentId}/attendance")
    public ResponseEntity<AttendanceResponse> attendance(
            @PathVariable UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(parentService.attendance(CurrentUser.require(), studentId, from, to));
    }

    @GetMapping("/children/{studentId}/observations")
    public ResponseEntity<List<ObservationDto>> observations(@PathVariable UUID studentId) {
        return ResponseEntity.ok(parentService.observations(CurrentUser.require(), studentId));
    }

    @GetMapping("/meetings")
    public ResponseEntity<List<MeetingDto>> meetings() {
        return ResponseEntity.ok(parentService.meetings(CurrentUser.require()));
    }

    @PostMapping("/meetings/{meetingId}/confirm")
    public ResponseEntity<MeetingDto> confirmMeeting(@PathVariable UUID meetingId,
                                                     @Valid @RequestBody MeetingConfirmRequest request) {
        return ResponseEntity.ok(parentService.confirmMeeting(CurrentUser.require(), meetingId, request));
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
}
