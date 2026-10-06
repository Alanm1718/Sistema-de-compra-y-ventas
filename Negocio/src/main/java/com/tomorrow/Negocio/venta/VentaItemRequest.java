package com.tomorrow.Negocio.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class VentaItemRequest {

    @NotNull(message = "Debés indicar el producto")
    private Long productId;

    @Positive(message = "La cantidad debe ser mayor a 0")
    private int cantidad;

    public VentaItemRequest() {
    }

    public VentaItemRequest(Long productId, int cantidad) {
        this.productId = productId;
        this.cantidad = cantidad;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
