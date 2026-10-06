package com.tomorrow.Negocio.security;

import com.tomorrow.Negocio.LoginAdmin.AdminRepository;
import com.tomorrow.Negocio.LoginAdmin.AdminUsuario;
import com.tomorrow.Negocio.LoginCliente.Cliente;
import com.tomorrow.Negocio.LoginCliente.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SessionAuthService {

    private final AdminRepository adminRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;

    public SessionAuthService(AdminRepository adminRepository,
                              ClienteRepository clienteRepository,
                              PasswordEncoder passwordEncoder,
                              SecurityContextRepository securityContextRepository) {
        this.adminRepository = adminRepository;
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public AuthenticatedUser loginAdmin(String username, String password,
                                        HttpServletRequest request, HttpServletResponse response) {
        AdminUsuario admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!matchesPassword(password, admin.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }
        if (!isEncoded(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(password));
            adminRepository.save(admin);
        }

        return establishSession(admin.getId(), admin.getUsername(), admin.getRole(), request, response);
    }

    @Transactional
    public AuthenticatedUser loginCliente(String username, String password,
                                         HttpServletRequest request, HttpServletResponse response) {
        Cliente cliente = clienteRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales de cliente incorrectas"));

        if (!matchesPassword(password, cliente.getPassword())) {
            throw new BadCredentialsException("Credenciales de cliente incorrectas");
        }
        if (!isEncoded(cliente.getPassword())) {
            cliente.setPassword(passwordEncoder.encode(password));
            clienteRepository.save(cliente);
        }

        return establishSession(cliente.getId(), cliente.getUsername(), "CLIENTE", request, response);
    }

    @Transactional
    public AuthenticatedUser registerCliente(String username, String password,
                                            HttpServletRequest request, HttpServletResponse response) {
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario debe tener entre 3 y 50 caracteres");
        }
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres y no superar 72 bytes");
        }
        if (clienteRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya existe");
        }

        Cliente cliente = clienteRepository.save(new Cliente(normalizedUsername, passwordEncoder.encode(password)));
        return establishSession(cliente.getId(), cliente.getUsername(), "CLIENTE", request, response);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (isEncoded(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isEncoded(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private AuthenticatedUser establishSession(Long id, String username, String role,
                                                HttpServletRequest request, HttpServletResponse response) {
        AuthenticatedUser principal = new AuthenticatedUser(id, username, role);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true);
        request.changeSessionId();
        securityContextRepository.saveContext(context, request, response);
        return principal;
    }
}