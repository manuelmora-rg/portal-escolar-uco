package com.portal.escolar.exceptions;

/** Regla de negocio incumplida (HTTP 422). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
