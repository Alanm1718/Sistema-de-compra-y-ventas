package com.tomorrow.Negocio.Pedidos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class PedidoRequest {

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    private List<@Valid PedidoItemRequest> items;

    public PedidoRequest() {
    }

    public PedidoRequest(List<PedidoItemRequest> items) {
        this.items = items;
    }

    public List<PedidoItemRequest> getItems() { return items; }
    public void setItems(List<PedidoItemRequest> items) { this.items = items; }
}