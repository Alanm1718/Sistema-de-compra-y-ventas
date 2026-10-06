package com.tomorrow.Negocio.Pedidos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Busca los pedidos filtrados por estado (ej: "PENDIENTE")
    List<Pedido> findByEstado(String estado);

    // Busca el historial de pedidos de un cliente específico
    List<Pedido> findByClienteId(Long clienteId);
}