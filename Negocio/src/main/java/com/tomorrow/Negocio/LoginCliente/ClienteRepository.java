package com.tomorrow.Negocio.LoginCliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Ahora sí busca y devuelve un cliente de la tabla clientes
    Optional<Cliente> findByUsername(String username);

}