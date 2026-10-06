package com.tomorrow.Negocio.venta;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public class VentaRequest {

    @NotNull(message = "Debés indicar el producto")
    private Long productId;

    @Positive(message = "La cantidad debe ser mayor a 0")
    private int cantidad;

    @NotBlank(message = "Debés indicar la forma de pago")
    @Pattern(regexp = "EFECTIVO|TRANSFERENCIA|TARJETA", message = "Forma de pago no válida")
    private String formaPago;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
}