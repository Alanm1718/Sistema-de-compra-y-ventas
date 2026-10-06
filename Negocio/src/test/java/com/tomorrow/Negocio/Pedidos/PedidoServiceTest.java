package com.tomorrow.Negocio.Pedidos;

import com.tomorrow.Negocio.LoginCliente.ClienteRepository;
import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.product.ProductRepository;
import com.tomorrow.Negocio.venta.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void aprobarPedidoRechazaUnidadesDuplicadasQueSuperanElStock() {
        Product product = productWithStock(5);
        Pedido pedido = pendingOrder(product, 4, 4);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        ResponseEntity<Object> response = pedidoService.aprobarPedido(1L);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(5, product.getStock());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void aprobarPedidoDescuentaLaSumaDeLineasDuplicadasUnaSolaVez() {
        Product product = productWithStock(10);
        Pedido pedido = pendingOrder(product, 2, 2);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        ResponseEntity<Object> response = pedidoService.aprobarPedido(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, product.getStock());
        verify(productRepository).save(product);
    }

    @Test
    void rechazarPedidoNoCancelaUnPedidoAprobado() {
        Pedido pedido = new Pedido();
        pedido.setEstado("APROBADO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        ResponseEntity<Object> response = pedidoService.rechazarPedido(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("APROBADO", pedido.getEstado());
        verify(pedidoRepository, never()).save(pedido);
    }

    @Test
    void crearPedidoRechazaCantidadesDuplicadasQueSuperanElStock() {
        Product product = productWithStock(5);
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(new com.tomorrow.Negocio.LoginCliente.Cliente("cliente", "clave")));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        PedidoRequest request = new PedidoRequest(List.of(
                new PedidoItemRequest(1L, 4),
                new PedidoItemRequest(1L, 4)));

        ResponseEntity<Object> response = pedidoService.crearPedido(2L, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    private Product productWithStock(int stock) {
        Product product = new Product("Producto", "General", 10, stock, 0, null);
        product.setId(1L);
        return product;
    }

    private Pedido pendingOrder(Product product, int... quantities) {
        List<PedidoItem> items = java.util.Arrays.stream(quantities)
                .mapToObj(quantity -> new PedidoItem(product, quantity, 10f))
                .toList();
        return new Pedido(null, null, 0f, "PENDIENTE", items);
    }
}