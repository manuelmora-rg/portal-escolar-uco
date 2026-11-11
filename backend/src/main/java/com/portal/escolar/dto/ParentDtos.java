package com.portal.escolar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public final class ParentDtos {

    public record ChildOverviewDto(
            UUID studentId,
            String fullName,
            String documentNumber,
            String gradeLevel,
            String groupName,
            String photoUrl,
            String relationship,
            BigDecimal average,
            BigDecimal attendanceRate,
            long pendingAssignments,
            long unreadNotifications
    ) {
    }

    public record MeetingConfirmRequest(
            @NotBlank(message = "El estado es obligatorio")
            @Pattern(regexp = "CONFIRMED|DECLINED", message = "El estado debe ser CONFIRMED o DECLINED")
            String status
    ) {
    }

    private ParentDtos() {
    }
}
