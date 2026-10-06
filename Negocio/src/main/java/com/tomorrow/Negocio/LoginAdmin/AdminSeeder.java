package com.tomorrow.Negocio.LoginAdmin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String vendorUsername;
    private final String vendorPassword;

    public AdminSeeder(AdminRepository adminRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${negocio.admin.username}") String adminUsername,
                       @Value("${negocio.admin.password}") String adminPassword,
                       @Value("${negocio.vendedor.username}") String vendorUsername,
                       @Value("${negocio.vendedor.password}") String vendorPassword) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.vendorUsername = vendorUsername;
        this.vendorPassword = vendorPassword;
    }

    @Override
    public void run(String... args) {
        boolean createdUser = false;
        if (adminRepository.findByUsername(adminUsername).isEmpty()) {
            adminRepository.save(new AdminUsuario(adminUsername, passwordEncoder.encode(adminPassword), "ADMIN"));
            createdUser = true;
        }
        if (adminRepository.findByUsername(vendorUsername).isEmpty()) {
            adminRepository.save(new AdminUsuario(vendorUsername, passwordEncoder.encode(vendorPassword), "VENDEDOR"));
            createdUser = true;
        }
        if (createdUser) {
            System.out.println(">> Se crearon las cuentas iniciales de administración y ventas.");
        }
    }
}