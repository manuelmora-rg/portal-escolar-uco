package com.portal.escolar.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    public static Optional<UserPrincipal> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UserPrincipal require() {
        return optional().orElseThrow(() ->
                new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException(
                        "No hay un usuario autenticado"));
    }

    private CurrentUser() {
    }
}
