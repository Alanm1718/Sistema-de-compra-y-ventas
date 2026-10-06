package com.tomorrow.Negocio.Pedidos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PedidoItemRequest {

    @NotNull(message = "El id del producto es obligatorio")
    private Long productId;

    @Positive(message = "La cantidad debe ser mayor a 0")
    private int cantidad;

    public PedidoItemRequest() {
    }

    public PedidoItemRequest(Long productId, int cantidad) {
        this.productId = productId;
        this.cantidad = cantidad;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}