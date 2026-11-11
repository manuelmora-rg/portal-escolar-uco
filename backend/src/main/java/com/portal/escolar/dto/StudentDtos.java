package com.portal.escolar.dto;

import jakarta.validation.constraints.Size;

public final class StudentDtos {

    public record SubmissionRequest(
            @Size(max = 1000, message = "El comentario no puede superar 1000 caracteres")
            String comment,

            @Size(max = 500, message = "La URL no puede superar 500 caracteres")
            String fileUrl
    ) {
    }

    private StudentDtos() {
    }
}
