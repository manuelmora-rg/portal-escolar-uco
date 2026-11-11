package com.portal.escolar.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "No encontrado", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> noResource(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "No encontrado", "El recurso solicitado no existe.");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> conflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> business(BusinessException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio", ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ProblemDetail> tooManyRequests(TooManyRequestsException ex) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> denied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", "No tienes permiso para acceder a este recurso.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> badCredentials(BadCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", "El correo o la contraseña no son correctos.");
    }

    @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ProblemDetail> notAuthenticated(Exception ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado", "Debes iniciar sesión.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los datos enviados.");
        detail.setTitle("Datos inválidos");
        detail.setType(URI.create("https://portal.escolar/errors/validation"));
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toError)
                .toList();
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception ex) {
        // No se exponen detalles internos al cliente.
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado.");
    }

    private Map<String, String> toError(FieldError error) {
        return Map.of("field", error.getField(), "message", error.getDefaultMessage() == null ? "inválido" : error.getDefaultMessage());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        return ResponseEntity.status(status).body(detail);
    }
}
