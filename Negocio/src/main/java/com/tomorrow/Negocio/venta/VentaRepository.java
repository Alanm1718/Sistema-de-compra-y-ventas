package com.tomorrow.Negocio.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByProductId(Long productId);

    List<Venta> findAllByOrderByFechaDesc();
}