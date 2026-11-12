package com.portal.escolar.integration;

import com.jayway.jsonpath.JsonPath;
import com.portal.escolar.support.ApiTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el control de acceso entre acudiente e hijo (IDOR) en el backend,
 * no solo la ocultación de opciones en el frontend.
 */
class ParentAccessIntegrationTest extends ApiTestSupport {

    @Test
    void acudientePuedeConsultarSoloSusHijosVinculados() throws Exception {
        String parentToken = login(PARENT_EMAIL);

        String children = getJson("/api/v1/parent/children", parentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        List<String> studentIds = JsonPath.read(children, "$[*].studentId");
        assertFalse(studentIds.isEmpty(), "El acudiente demo debe tener al menos un hijo vinculado");

        getJson("/api/v1/parent/children/" + studentIds.getFirst() + "/grades", parentToken)
                .andExpect(status().isOk());
    }

    @Test
    void acudienteNoPuedeConsultarUnEstudianteNoVinculado() throws Exception {
        String adminToken = login(ADMIN_EMAIL);
        String email = uniqueEmail("ajeno");
        String document = "AJENO-" + uniqueDigits();

        postJson("/api/v1/admin/users", adminToken, body(
                "firstName", "Estudiante",
                "lastName", "Ajeno",
                "email", email,
                "role", "STUDENT",
                "documentNumber", document))
                .andExpect(status().isCreated());

        String students = getJson("/api/v1/admin/students", adminToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> ids = JsonPath.read(students, "$[?(@.email=='" + email + "')].studentId");
        assertFalse(ids.isEmpty(), "El estudiante creado debe aparecer en el listado");
        String unlinkedStudentId = ids.getFirst();

        String parentToken = login(PARENT_EMAIL);

        getJson("/api/v1/parent/children/" + unlinkedStudentId + "/grades", parentToken)
                .andExpect(status().isForbidden());

        getJson("/api/v1/parent/children/" + unlinkedStudentId + "/attendance", parentToken)
                .andExpect(status().isForbidden());

        getJson("/api/v1/parent/children/" + unlinkedStudentId + "/overview", parentToken)
                .andExpect(status().isForbidden());
    }

    @Test
    void estudianteNoPuedeUsarEndpointsDeAcudiente() throws Exception {
        String studentToken = login(STUDENT_EMAIL);

        getJson("/api/v1/parent/children", studentToken)
                .andExpect(status().isForbidden());
    }

    @Test
    void acudienteNoPuedeAccederAlLibroDeCalificaciones() throws Exception {
        String parentToken = login(PARENT_EMAIL);

        // Aunque conozca un identificador, el endpoint de docencia está reservado al rol TEACHER.
        getJson("/api/v1/teacher/course-subjects/00000000-0000-0000-0000-000000000000/gradebook",
                parentToken)
                .andExpect(status().isForbidden());
    }

    @Test
    void estudianteNoPuedeRegistrarSusPropiasCalificaciones() throws Exception {
        String studentToken = login(STUDENT_EMAIL);

        getJson("/api/v1/teacher/course-subjects/00000000-0000-0000-0000-000000000000/gradebook",
                studentToken)
                .andExpect(status().isForbidden());
    }
}
