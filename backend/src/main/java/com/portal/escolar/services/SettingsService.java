package com.portal.escolar.services;

import com.portal.escolar.dto.AcademicDtos.PublicInstitutionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final JdbcTemplate jdbcTemplate;

    public PublicInstitutionDto institution() {
        String sql = """
                select setting_value->>'name'           as name,
                       setting_value->>'shortName'      as short_name,
                       setting_value->>'slogan'         as slogan,
                       setting_value->>'mission'        as mission,
                       setting_value->>'vision'         as vision,
                       setting_value->>'history'        as history,
                       setting_value->>'address'        as address,
                       setting_value->>'phone'          as phone,
                       setting_value->>'email'          as email,
                       setting_value->>'primaryColor'   as primary_color,
                       setting_value->>'accentColor'    as accent_color,
                       setting_value->>'logoUrl'        as logo_url
                from portal.site_setting
                where setting_key = 'institution'
                """;
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new PublicInstitutionDto(
                    rs.getString("name"),
                    rs.getString("short_name"),
                    rs.getString("slogan"),
                    rs.getString("mission"),
                    rs.getString("vision"),
                    rs.getString("history"),
                    rs.getString("address"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("primary_color"),
                    rs.getString("accent_color"),
                    rs.getString("logo_url")
            ));
        } catch (EmptyResultDataAccessException ex) {
            return new PublicInstitutionDto("Portal Escolar", "Portal Escolar", "", "", "", "",
                    "", "", "", "#0b2545", "#38bdf8", null);
        }
    }

    public void updateInstitution(String json) {
        jdbcTemplate.update("""
                update portal.site_setting
                set setting_value = ?::jsonb, updated_at = now()
                where setting_key = 'institution'
                """, json);
    }
}
