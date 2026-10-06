package com.tomorrow.Negocio.security;

public record AuthenticatedUser(Long id, String username, String role) {
}