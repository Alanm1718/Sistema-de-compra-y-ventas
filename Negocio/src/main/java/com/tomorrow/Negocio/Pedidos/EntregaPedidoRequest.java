package com.tomorrow.Negocio.Pedidos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EntregaPedidoRequest {

    @NotBlank(message = "Debés indicar la forma de pago")
    @Pattern(regexp = "EFECTIVO|TRANSFERENCIA|TARJETA", message = "Forma de pago no válida")
    private String formaPago;

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }
}