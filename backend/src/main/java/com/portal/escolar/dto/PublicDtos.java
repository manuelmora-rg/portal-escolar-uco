package com.portal.escolar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class PublicDtos {

    public record ContactRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120)
            String fullName,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 160)
            String email,

            @Size(max = 30)
            String phone,

            @NotBlank(message = "El asunto es obligatorio")
            @Size(max = 150)
            String subject,

            @NotBlank(message = "El mensaje es obligatorio")
            @Size(max = 2000)
            String message
    ) {
    }

    public record AcademicOfferDto(
            java.util.List<Level> levels,
            java.util.List<Subject> subjects
    ) {
        public record Level(String name, int order) {
        }

        public record Subject(String code, String name, String area) {
        }
    }

    private PublicDtos() {
    }
}
