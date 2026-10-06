package com.tomorrow.Negocio.LoginCliente;

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
@RequestMapping("/api/v1/auth/cliente")
public class ClienteAuthController {

    private final SessionAuthService sessionAuthService;

    public ClienteAuthController(SessionAuthService sessionAuthService) {
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody ClienteLoginRequest request,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
        try {
            AuthenticatedUser user = sessionAuthService.loginCliente(request.getUsername(), request.getPassword(),
                    servletRequest, servletResponse);
            return ResponseEntity.ok(Map.of(
                    "error", false,
                    "mensaje", "Bienvenido Cliente",
                    "clienteId", user.id(),
                    "username", user.username(),
                    "role", user.role()));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", true, "mensaje", "Credenciales de cliente incorrectas"));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<Object> registro(@Valid @RequestBody ClienteLoginRequest request,
                                          HttpServletRequest servletRequest,
                                          HttpServletResponse servletResponse) {
        AuthenticatedUser user = sessionAuthService.registerCliente(request.getUsername(), request.getPassword(),
                servletRequest, servletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Cuenta creada correctamente",
                "clienteId", user.id(),
                "username", user.username(),
                "role", user.role()));
    }
}