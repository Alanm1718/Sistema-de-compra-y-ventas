package com.tomorrow.Negocio.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class VentaManualRequest {

    @NotEmpty(message = "Debés cargar al menos un producto")
    @Valid
    private List<VentaItemRequest> items;

    @NotBlank(message = "Debés indicar la forma de pago")
    @Pattern(regexp = "EFECTIVO|TRANSFERENCIA|TARJETA", message = "Forma de pago no válida")
    private String formaPago;

    public List<VentaItemRequest> getItems() {
        return items;
    }

    public void setItems(List<VentaItemRequest> items) {
        this.items = items;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }
}
