package com.tomorrow.Negocio.LoginAdmin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminUsuario, Long> {
    Optional<AdminUsuario> findByUsername(String username);
}