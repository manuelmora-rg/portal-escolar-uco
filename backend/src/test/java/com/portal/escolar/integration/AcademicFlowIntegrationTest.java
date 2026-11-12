package com.portal.escolar.integration;

import com.jayway.jsonpath.JsonPath;
import com.portal.escolar.support.ApiTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo académico real: el docente registra nota y asistencia, crea una tarea,
 * y el estudiante la entrega. Verifica persistencia y reglas de acceso.
 */
class AcademicFlowIntegrationTest extends ApiTestSupport {

    @Test
    void docenteCalificaRegistraAsistenciaYCreaTareaQueElEstudianteEntrega() throws Exception {
        String teacherToken = login(TEACHER_EMAIL);
        String studentToken = login(STUDENT_EMAIL);

        String profile = getJson("/api/v1/student/profile", studentToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String studentId = JsonPath.read(profile, "$.id");

        String subjects = getJson("/api/v1/teacher/course-subjects", teacherToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int subjectCount = JsonPath.read(subjects, "$.length()");
        String courseSubjectId = null;
        String periodId = null;
        String enrollmentId = null;

        // Busca una asignatura del docente en la que esté matriculado el estudiante demo.
        for (int i = 0; i < subjectCount; i++) {
            String candidateId = JsonPath.read(subjects, "$[" + i + "].id");
            String gradebook = getJson("/api/v1/teacher/course-subjects/" + candidateId + "/gradebook",
                    teacherToken)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<String> enrolled = JsonPath.read(gradebook, "$.students[*].studentId");
            if (enrolled.contains(studentId)) {
                courseSubjectId = candidateId;
                periodId = JsonPath.read(gradebook, "$.period.id");
                List<String> enrollmentIds = JsonPath.read(gradebook,
                        "$.students[?(@.studentId=='" + studentId + "')].enrollmentId");
                enrollmentId = enrollmentIds.getFirst();
                break;
            }
        }

        assertFalse(courseSubjectId == null, "El docente demo debe compartir una asignatura con el estudiante");

        String gradeTitle = "Nota de integración " + UUID.randomUUID().toString().substring(0, 8);
        String gradeBody = postJson("/api/v1/teacher/course-subjects/" + courseSubjectId + "/grades",
                teacherToken, Map.of("entries", List.of(Map.of(
                        "enrollmentId", enrollmentId,
                        "periodId", periodId,
                        "title", gradeTitle,
                        "type", "EXAM",
                        "score", 4.0,
                        "weight", 10.0,
                        "comment", "Registro automatizado de prueba"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> gradeIds = JsonPath.read(gradeBody,
                "$..grades[?(@.title=='" + gradeTitle + "')].id");
        assertFalse(gradeIds.isEmpty(), "La calificación creada debe aparecer en el cuaderno de notas");
        String gradeId = gradeIds.getFirst();

        putJson("/api/v1/teacher/grades/" + gradeId, teacherToken, Map.of(
                "enrollmentId", enrollmentId,
                "periodId", periodId,
                "title", gradeTitle,
                "type", "EXAM",
                "score", 4.5,
                "weight", 10.0,
                "comment", "Calificación modificada por el docente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score", is(4.5)));

        // El estudiante ve la nota actualizada.
        getJson("/api/v1/student/grades", studentToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..grades[?(@.title=='" + gradeTitle + "')].score", hasItem(4.5)));

        postJson("/api/v1/teacher/course-subjects/" + courseSubjectId + "/attendance",
                teacherToken, Map.of(
                        "date", LocalDate.now().toString(),
                        "entries", List.of(Map.of(
                                "enrollmentId", enrollmentId,
                                "status", "PRESENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[?(@.enrollmentId=='" + enrollmentId + "')].status",
                        hasItem("PRESENT")));

        String assignmentTitle = "Tarea de integración " + UUID.randomUUID().toString().substring(0, 8);
        String assignmentBody = postJson("/api/v1/teacher/course-subjects/" + courseSubjectId + "/assignments",
                teacherToken, Map.of(
                        "title", assignmentTitle,
                        "description", "Actividad creada por una prueba automatizada",
                        "dueAt", Instant.now().plus(7, ChronoUnit.DAYS).toString(),
                        "maxScore", 5.0,
                        "publish", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(assignmentTitle)))
                .andReturn().getResponse().getContentAsString();

        String assignmentId = JsonPath.read(assignmentBody, "$.id");

        postJson("/api/v1/student/assignments/" + assignmentId + "/submit", studentToken, Map.of(
                "comment", "Entrega realizada por la prueba de integración"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", anyOf(is("SUBMITTED"), is("LATE"))));

        getJson("/api/v1/teacher/assignments/" + assignmentId + "/submissions", teacherToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].studentId", hasItem(studentId)));
    }
}
