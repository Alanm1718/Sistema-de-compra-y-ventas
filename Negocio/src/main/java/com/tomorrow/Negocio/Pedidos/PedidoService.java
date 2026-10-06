package com.tomorrow.Negocio.Pedidos;

import com.tomorrow.Negocio.LoginCliente.Cliente;
import com.tomorrow.Negocio.LoginCliente.ClienteRepository;
import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.product.ProductRepository;
import com.tomorrow.Negocio.venta.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductRepository productRepository;
    private final ClienteRepository clienteRepository;
    private final VentaService ventaService;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository,
                         ProductRepository productRepository,
                         ClienteRepository clienteRepository,
                         VentaService ventaService) {
        this.pedidoRepository = pedidoRepository;
        this.productRepository = productRepository;
        this.clienteRepository = clienteRepository;
        this.ventaService = ventaService;
    }

    // 1. Obtener todos los pedidos pendientes de revisión por el dueño
    public List<Pedido> getPedidosPendientes() {
        return pedidoRepository.findByEstado("PENDIENTE");
    }

    // 2. Obtener el historial de pedidos de un cliente
    public List<Pedido> getPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    // 3. Crear un nuevo pedido enviado por el cliente
    @Transactional
    public ResponseEntity<Object> crearPedido(Long clienteId, PedidoRequest request) {
        Map<String, Object> body = new HashMap<>();

        // Validar que el cliente exista
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "El cliente no existe");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        List<PedidoItem> listaItems = new ArrayList<>();
        Map<Long, Long> cantidadesPorProducto = new HashMap<>();
        Map<Long, Product> productosPorId = new HashMap<>();
        float totalGeneral = 0.0f;

        // Procesar cada ítem enviado en el carrito
        for (PedidoItemRequest itemReq : request.getItems()) {
            Optional<Product> productOpt = productRepository.findById(itemReq.getProductId());
            if (productOpt.isEmpty()) {
                body.put("error", true);
                body.put("mensaje", "El producto ID " + itemReq.getProductId() + " no existe");
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
            }

            Product product = productOpt.get();
            cantidadesPorProducto.merge(product.getId(), (long) itemReq.getCantidad(), Long::sum);
            productosPorId.put(product.getId(), product);

            // Guardar el precio actual del producto para proteger la historia contable
            PedidoItem item = new PedidoItem(product, itemReq.getCantidad(), product.getPrice());
            listaItems.add(item);

            totalGeneral += item.getSubtotal();
        }

        for (Map.Entry<Long, Long> entry : cantidadesPorProducto.entrySet()) {
            Product product = productosPorId.get(entry.getKey());
            if (entry.getValue() > product.getStock()) {
                body.put("error", true);
                body.put("mensaje", "Stock insuficiente para " + product.getName()
                        + ". Disponible: " + product.getStock());
                return new ResponseEntity<>(body, HttpStatus.CONFLICT);
            }
        }

        // Crear la entidad Pedido en estado PENDIENTE
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteOpt.get());
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(totalGeneral);
        pedido.setEstado("PENDIENTE");
        pedido.setItems(listaItems);

        Pedido guardado = pedidoRepository.save(pedido);

        body.put("mensaje", "Pedido creado con éxito. En espera de confirmación.");
        body.put("data", guardado);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    // 4. El dueño APRUEBA el pedido (Descuenta stock)
    @Transactional
    public ResponseEntity<Object> aprobarPedido(Long pedidoId) {
        Map<String, Object> body = new HashMap<>();

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "El pedido no existe");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Pedido pedido = pedidoOpt.get();

        if (!"PENDIENTE".equals(pedido.getEstado())) {
            body.put("error", true);
            body.put("mensaje", "Solo se pueden aprobar pedidos en estado PENDIENTE");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        Map<Long, Long> cantidadesPorProducto = new HashMap<>();
        Map<Long, Product> productosPorId = new HashMap<>();
        for (PedidoItem item : pedido.getItems()) {
            Product product = item.getProduct();
            cantidadesPorProducto.merge(product.getId(), (long) item.getCantidad(), Long::sum);
            productosPorId.put(product.getId(), product);
        }

        // Validar el total por producto antes de actualizar el inventario.
        for (Map.Entry<Long, Long> entry : cantidadesPorProducto.entrySet()) {
            Product product = productosPorId.get(entry.getKey());
            if (product.getStock() < entry.getValue()) {
                body.put("error", true);
                body.put("mensaje", "No hay suficiente stock para el producto: " + product.getName() +
                        " (Stock actual: " + product.getStock() + ")");
                return new ResponseEntity<>(body, HttpStatus.CONFLICT);
            }
        }

        // Descontar una sola vez por producto para evitar duplicar actualizaciones.
        for (Map.Entry<Long, Long> entry : cantidadesPorProducto.entrySet()) {
            Product product = productosPorId.get(entry.getKey());
            product.setStock(product.getStock() - entry.getValue().intValue());
            productRepository.save(product);
        }

        // Cambiar el estado del pedido a APROBADO
        pedido.setEstado("APROBADO");
        pedidoRepository.save(pedido);

        body.put("mensaje", "Pedido aprobado y stock actualizado correctamente");
        body.put("data", pedido);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // 5. El dueño RECHAZA / CANCELA el pedido (No toca el stock)
    public ResponseEntity<Object> rechazarPedido(Long pedidoId) {
        Map<String, Object> body = new HashMap<>();

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "El pedido no existe");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Pedido pedido = pedidoOpt.get();
        if (!"PENDIENTE".equals(pedido.getEstado())) {
            body.put("error", true);
            body.put("mensaje", "Solo se pueden cancelar pedidos en estado PENDIENTE");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        pedido.setEstado("CANCELADO");
        pedidoRepository.save(pedido);

        body.put("mensaje", "Pedido cancelado");
        body.put("data", pedido);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public List<Pedido> getPedidosEnPreparacion() {
        return pedidoRepository.findByEstado("APROBADO");
    }

    public List<Pedido> getPedidosListos() {
        return pedidoRepository.findByEstado("LISTO");
    }

    public ResponseEntity<Object> marcarListo(Long pedidoId) {
        Map<String, Object> body = new HashMap<>();

        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "El pedido no existe");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Pedido pedido = pedidoOpt.get();
        if (!"APROBADO".equals(pedido.getEstado())) {
            body.put("error", true);
            body.put("mensaje", "Solo se puede marcar como listo un pedido ya aprobado");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        pedido.setEstado("LISTO");
        pedidoRepository.save(pedido);

        body.put("mensaje", "Pedido marcado como listo para retirar");
        body.put("data", pedido);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Object> entregarPedido(Long pedidoId, String formaPago) {
        Map<String, Object> body = new HashMap<>();
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "El pedido no existe");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Pedido pedido = pedidoOpt.get();
        if (!"LISTO".equals(pedido.getEstado())) {
            body.put("error", true);
            body.put("mensaje", "Solo se puede registrar la venta de un pedido LISTO");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        ventaService.registrarPedidoEntregado(pedido, formaPago);
        pedido.setEstado("ENTREGADO");
        pedidoRepository.save(pedido);

        body.put("mensaje", "Pedido entregado y venta registrada");
        body.put("data", pedido);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}