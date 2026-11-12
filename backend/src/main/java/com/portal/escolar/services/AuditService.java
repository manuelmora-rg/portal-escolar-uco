package com.portal.escolar.services;

import com.portal.escolar.dto.AdminDtos.AuditLogDto;
import com.portal.escolar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registro de auditoría general. Usa SQL parametrizado para no concatenar valores.
 * Un fallo al auditar no debe tumbar la operación de negocio.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void record(UserPrincipal actor, String action, String entity, String entityId,
                       Map<String, Object> details) {
        try {
            String json = details == null || details.isEmpty() ? null : objectMapper.writeValueAsString(details);
            jdbcTemplate.update("""
                            insert into portal.audit_log (id, user_id, action, entity, entity_id, details, ip, created_at)
                            values (?, ?, ?, ?, ?, ?::jsonb, ?, now())
                            """,
                    UUID.randomUUID(),
                    actor == null ? null : actor.id(),
                    action,
                    entity,
                    entityId,
                    json,
                    clientIp());
        } catch (Exception ex) {
            log.warn("No fue posible registrar la auditoría {} sobre {}", action, entity, ex);
        }
    }

    public List<AuditLogDto> list(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return jdbcTemplate.query("""
                        select a.id, u.first_name, u.last_name, a.action, a.entity, a.entity_id,
                               a.details::text as details, a.ip, a.created_at
                        from portal.audit_log a
                        left join portal.app_user u on u.id = a.user_id
                        order by a.created_at desc
                        limit ?
                        """,
                (rs, rowNum) -> {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String actor = firstName == null ? null : (firstName + " " + lastName).trim();
                    Timestamp created = rs.getTimestamp("created_at");
                    Instant createdAt = created == null ? null : created.toInstant();
                    return new AuditLogDto(
                            rs.getObject("id", UUID.class),
                            actor,
                            rs.getString("action"),
                            rs.getString("entity"),
                            rs.getString("entity_id"),
                            rs.getString("details"),
                            rs.getString("ip"),
                            createdAt
                    );
                },
                safeLimit);
    }

    private String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String ip = attributes.getRequest().getRemoteAddr();
            if (ip != null && ip.length() > 45) {
                return ip.substring(0, 45);
            }
            return ip;
        }
        return null;
    }
}
