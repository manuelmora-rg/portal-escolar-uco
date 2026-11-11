package com.portal.escolar.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Fallback para la aplicación de una sola página (React) en producción.
 * Las rutas conocidas y cualquier ruta sin extensión se resuelven con el index.html;
 * las rutas de API, actuator y los archivos estáticos no se interceptan.
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/institucion",
            "/oferta-academica",
            "/noticias",
            "/noticias/{slug}",
            "/galeria",
            "/calendario",
            "/contacto",
            "/login",
            "/app",
            "/app/**"
    })
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Sirve la SPA para rutas del navegador no listadas arriba (por ejemplo /ruta-inexistente).
     * El patrón excluye segmentos con punto para no capturar archivos estáticos.
     */
    @GetMapping({"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public Object fallback(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/") || path.startsWith("/actuator/")) {
            return ResponseEntity.notFound().build();
        }
        return "forward:/index.html";
    }
}
