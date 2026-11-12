package com.portal.escolar.integration;

import com.jayway.jsonpath.JsonPath;
import com.portal.escolar.support.ApiTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IdentityAdminIntegrationTest extends ApiTestSupport {

    @Test
    void administradorCreaDocenteYApareceEnElListado() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("docente.nuevo");

        String created = postJson("/api/v1/admin/users", token, body(
                "firstName", "Docente",
                "lastName", "Prueba",
                "email", email,
                "role", "TEACHER",
                "employeeCode", "EMP-" + uniqueDigits()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email", is(email)))
                .andExpect(jsonPath("$.user.role", is("TEACHER")))
                .andExpect(jsonPath("$.temporaryPassword", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String temporaryPassword = JsonPath.read(created, "$.temporaryPassword");

        // La contraseña temporal generada debe permitir iniciar sesión.
        login(email, temporaryPassword);

        getJson("/api/v1/admin/users", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)));
    }

    @Test
    void administradorCreaEstudianteConPerfilAcademico() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("estudiante.nuevo");
        String document = "DOC-" + uniqueDigits();

        postJson("/api/v1/admin/users", token, body(
                "firstName", "Estudiante",
                "lastName", "Prueba",
                "email", email,
                "role", "STUDENT",
                "documentNumber", document))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role", is("STUDENT")));

        getJson("/api/v1/admin/students", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)))
                .andExpect(jsonPath("$[*].documentNumber", hasItem(document)));
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("desactivado");
        String password = "Temporal2026*";

        String created = postJson("/api/v1/admin/users", token, body(
                "firstName", "Cuenta",
                "lastName", "Desactivada",
                "email", email,
                "role", "PARENT",
                "temporaryPassword", password))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userId = JsonPath.read(created, "$.user.id");

        patchJson("/api/v1/admin/users/" + userId + "/status", token, Map.of("enabled", false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled", is(false)));

        postJson("/api/v1/auth/login", null, Map.of("email", email, "password", password))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void administradorNoPuedeDesactivarSuPropiaCuenta() throws Exception {
        String token = login(ADMIN_EMAIL);

        String me = getJson("/api/v1/auth/me", token)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String adminId = JsonPath.read(me, "$.id");

        patchJson("/api/v1/admin/users/" + adminId + "/status", token, Map.of("enabled", false))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void restablecerContrasenaInvalidaLaAnterior() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("reset");
        String original = "Original2026*";
        String nueva = "NuevaClave2026*";

        String created = postJson("/api/v1/admin/users", token, body(
                "firstName", "Reset",
                "lastName", "Prueba",
                "email", email,
                "role", "PARENT",
                "temporaryPassword", original))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userId = JsonPath.read(created, "$.user.id");

        postJson("/api/v1/admin/users/" + userId + "/reset-password", token,
                Map.of("temporaryPassword", nueva))
                .andExpect(status().isOk());

        login(email, nueva);

        postJson("/api/v1/auth/login", null, Map.of("email", email, "password", original))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invitacionSeAceptaUnaSolaVezYPermiteIniciarSesion() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("invitado");
        String employeeCode = "INV-" + uniqueDigits();
        String password = "Invitado2026*";

        String created = postJson("/api/v1/admin/invitations", token,
                Map.of("email", email, "role", "TEACHER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitation.email", is(email)))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        String invitationToken = JsonPath.read(created, "$.token");

        // La información de la invitación es pública, pero no expone el token ni datos privados.
        getJson("/api/v1/auth/invitations/" + invitationToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.role", is("TEACHER")))
                .andExpect(jsonPath("$.valid", is(true)));

        postJson("/api/v1/auth/accept-invitation", null, body(
                "token", invitationToken,
                "firstName", "Invitado",
                "lastName", "Prueba",
                "password", password,
                "employeeCode", employeeCode))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.role", is("TEACHER")));

        login(email, password);

        // Una invitación usada ya no se puede consultar ni reutilizar.
        getJson("/api/v1/auth/invitations/" + invitationToken, null)
                .andExpect(status().isNotFound());

        postJson("/api/v1/auth/accept-invitation", null, body(
                "token", invitationToken,
                "firstName", "Invitado",
                "lastName", "Prueba",
                "password", password,
                "employeeCode", employeeCode))
                .andExpect(status().isNotFound());
    }

    @Test
    void auditoriaRegistraLasAccionesAdministrativas() throws Exception {
        String token = login(ADMIN_EMAIL);
        String email = uniqueEmail("auditoria");

        postJson("/api/v1/admin/users", token, body(
                "firstName", "Auditoria",
                "lastName", "Prueba",
                "email", email,
                "role", "PARENT"))
                .andExpect(status().isCreated());

        getJson("/api/v1/admin/audit-logs", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].action", hasItem("USER_CREATED")))
                .andExpect(jsonPath("$[0].action", notNullValue()));
    }
}
