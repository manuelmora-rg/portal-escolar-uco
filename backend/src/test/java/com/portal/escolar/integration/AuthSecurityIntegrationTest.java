package com.portal.escolar.integration;

import com.portal.escolar.support.ApiTestSupport;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSecurityIntegrationTest extends ApiTestSupport {

    @Test
    void loginCorrectoDevuelveTokenYPerfil() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_EMAIL, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("portal_refresh"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email", is(ADMIN_EMAIL)))
                .andExpect(jsonPath("$.user.role", is("ADMIN")));
    }

    @Test
    void credencialesInvalidasDevuelven401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_EMAIL, "clave-incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accesoAnonimoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void estudianteNoPuedeAccederAEndpointsDeAdministrador() throws Exception {
        String token = login(STUDENT_EMAIL);

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/audit-logs").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void docenteNoPuedeAccederAEndpointsDeEstudiante() throws Exception {
        String token = login(TEACHER_EMAIL);

        mockMvc.perform(get("/api/v1/student/grades").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void acudienteNoPuedeAccederAEndpointsDeDocente() throws Exception {
        String token = login(PARENT_EMAIL);

        mockMvc.perform(get("/api/v1/teacher/dashboard").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAccedeAGestionDeUsuarios() throws Exception {
        String token = login(ADMIN_EMAIL);

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ADMIN")));

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void refreshRotaElTokenYRechazaElAnterior() throws Exception {
        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(credentials(ADMIN_EMAIL, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie original = loginResult.getResponse().getCookie("portal_refresh");
        org.junit.jupiter.api.Assertions.assertNotNull(original);

        var refreshResult = mockMvc.perform(post("/api/v1/auth/refresh").cookie(original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Cookie rotated = refreshResult.getResponse().getCookie("portal_refresh");
        org.junit.jupiter.api.Assertions.assertNotNull(rotated);
        org.junit.jupiter.api.Assertions.assertNotEquals(original.getValue(), rotated.getValue());

        // El refresh anterior ya fue revocado: reutilizarlo debe fallar.
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(original))
                .andExpect(status().isUnauthorized());
    }
}
