package com.tomorrow.Negocio.security;

import com.tomorrow.Negocio.LoginAdmin.AdminRepository;
import com.tomorrow.Negocio.LoginAdmin.AdminUsuario;
import com.tomorrow.Negocio.LoginCliente.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionAuthServiceTest {

    @Test
    void loginMigraLaContrasenaAnteriorYEstableceRolAdminEnSesion() {
        AdminRepository adminRepository = mock(AdminRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        SecurityContextRepository contextRepository = mock(SecurityContextRepository.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(true)).thenReturn(mock(HttpSession.class));

        AdminUsuario admin = new AdminUsuario("admin", "clave-antigua", "ADMIN");
        admin.setId(7L);
        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode("clave-antigua")).thenReturn("$2b$hash-nuevo");
        SessionAuthService authService = new SessionAuthService(
                adminRepository, clienteRepository, passwordEncoder, contextRepository);

        AuthenticatedUser user = authService.loginAdmin("admin", "clave-antigua", request, response);

        assertEquals(7L, user.id());
        assertEquals("ADMIN", user.role());
        assertEquals("$2b$hash-nuevo", admin.getPassword());
        verify(adminRepository).save(admin);
        verify(contextRepository).saveContext(any(), eq(request), eq(response));
    }
}