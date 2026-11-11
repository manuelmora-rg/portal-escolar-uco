package com.portal.escolar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {

    public record LoginRequest(
            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password
    ) {
    }

    public record UserSummary(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String fullName,
            String role,
            boolean mustChangePassword,
            UUID profileId
    ) {
    }

    public record AuthResponse(
            String accessToken,
            long expiresInSeconds,
            UserSummary user
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "La contraseña actual es obligatoria")
            String currentPassword,

            @NotBlank(message = "La nueva contraseña es obligatoria")
            @Size(min = 10, max = 72, message = "La contraseña debe tener entre 10 y 72 caracteres")
            String newPassword
    ) {
    }

    private AuthDtos() {
    }
}
