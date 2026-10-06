package com.tomorrow.Negocio.Pedidos;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.tomorrow.Negocio.security.AuthenticatedUser;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // 1. POST http://localhost:8080/api/pedidos
    // El cliente envía su carrito de compras
    @PostMapping
    public ResponseEntity<Object> crearPedido(@Valid @RequestBody PedidoRequest request,
                                              @AuthenticationPrincipal AuthenticatedUser cliente) {
        return pedidoService.crearPedido(cliente.id(), request);
    }

    // 2. GET http://localhost:8080/api/pedidos/pendientes
    // El dueño consulta los pedidos que esperan aprobación
    @GetMapping("/pendientes")
    public List<Pedido> getPedidosPendientes() {
        return pedidoService.getPedidosPendientes();
    }

    @GetMapping("/preparacion")
    public List<Pedido> getPedidosEnPreparacion() {
        return pedidoService.getPedidosEnPreparacion();
    }

    @GetMapping("/listos")
    public List<Pedido> getPedidosListos() {
        return pedidoService.getPedidosListos();
    }

    // 3. GET http://localhost:8080/api/pedidos/cliente/1
    // El cliente consulta su historial de compras pasadas
    @GetMapping("/cliente")
    public List<Pedido> getMisPedidos(@AuthenticationPrincipal AuthenticatedUser cliente) {
        return pedidoService.getPedidosPorCliente(cliente.id());
    }

    // 4. PUT http://localhost:8080/api/pedidos/1/aprobar
    // El dueño aprueba el pedido y se descuenta el stock en BD
    @PutMapping("/{pedidoId}/aprobar")
    public ResponseEntity<Object> aprobarPedido(@PathVariable Long pedidoId) {
        return pedidoService.aprobarPedido(pedidoId);
    }

    // 5. PUT http://localhost:8080/api/pedidos/1/rechazar
    // El dueño rechaza/cancela el pedido
    @PutMapping("/{pedidoId}/rechazar")
    public ResponseEntity<Object> rechazarPedido(@PathVariable Long pedidoId) {
        return pedidoService.rechazarPedido(pedidoId);
    }

    @PutMapping("/{pedidoId}/listo")
    public ResponseEntity<Object> marcarListo(@PathVariable Long pedidoId) {
        return pedidoService.marcarListo(pedidoId);
    }

    @PutMapping("/{pedidoId}/entregar")
    public ResponseEntity<Object> entregarPedido(@PathVariable Long pedidoId,
                                                 @Valid @RequestBody EntregaPedidoRequest request) {
        return pedidoService.entregarPedido(pedidoId, request.getFormaPago());
    }
}