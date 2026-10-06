package com.tomorrow.Negocio.LoginAdmin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import com.tomorrow.Negocio.security.AuthenticatedUser;
import com.tomorrow.Negocio.security.SessionAuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/admin")
public class AdminAuthController {

    private final SessionAuthService sessionAuthService;

    public AdminAuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody AdminLoginRequest request,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
        try {
            AuthenticatedUser user = sessionAuthService.loginAdmin(request.getUsername(), request.getPassword(),
                    servletRequest, servletResponse);
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "mensaje", "Bienvenido",
                    "adminId", user.id(),
                    "username", user.username(),
                    "role", user.role()));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "mensaje", "Usuario o contraseña incorrectos"));
        }
    }
}