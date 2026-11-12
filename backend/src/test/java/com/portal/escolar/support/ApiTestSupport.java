package com.portal.escolar.support;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base para pruebas de integración HTTP. Usa el perfil `test`, que apunta a la
 * base de datos portal_escolar_test y carga los datos ficticios del portal.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestSupport {

    protected static final String DEMO_PASSWORD = "Portal2026*";
    protected static final String ADMIN_EMAIL = "admin@portalescolar.edu.co";
    protected static final String TEACHER_EMAIL = "laura.gomez@portalescolar.edu.co";
    protected static final String STUDENT_EMAIL = "sofia.mora@portalescolar.edu.co";
    protected static final String PARENT_EMAIL = "maria.giraldo@portalescolar.edu.co";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected String login(String email) throws Exception {
        return login(email, DEMO_PASSWORD);
    }

    protected String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    protected String credentials(String email, String password) {
        return json(Map.of("email", email, "password", password));
    }

    protected String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    /** Construye un cuerpo JSON omitiendo valores nulos. */
    protected Map<String, Object> body(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                map.put((String) keyValues[i], value);
            }
        }
        return map;
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@example.test";
    }

    protected static String uniqueDigits() {
        return String.valueOf(System.nanoTime()).substring(0, 9);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }

    // ===== Helpers HTTP para mantener las pruebas legibles =====

    protected ResultActions getJson(String url, String token) throws Exception {
        MockHttpServletRequestBuilder request = get(url);
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        return mockMvc.perform(request);
    }

    protected ResultActions postJson(String url, String token, Object payload) throws Exception {
        return sendJson(post(url), token, payload);
    }

    protected ResultActions patchJson(String url, String token, Object payload) throws Exception {
        return sendJson(patch(url), token, payload);
    }

    protected ResultActions putJson(String url, String token, Object payload) throws Exception {
        return sendJson(put(url), token, payload);
    }

    private ResultActions sendJson(MockHttpServletRequestBuilder request, String token, Object payload)
            throws Exception {
        if (token != null) {
            request.header("Authorization", bearer(token));
        }
        request.contentType(MediaType.APPLICATION_JSON);
        if (payload != null) {
            request.content(json(payload));
        }
        return mockMvc.perform(request);
    }
}
