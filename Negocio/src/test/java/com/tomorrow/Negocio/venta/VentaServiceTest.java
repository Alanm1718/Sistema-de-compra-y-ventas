package com.tomorrow.Negocio.venta;

import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void registrarVentaManualDescuentaStockYCalculaTotal() {
        Product agua = new Product("Agua", "Bebidas", 2500f, 5, 0, null);
        agua.setId(1L);
        Product pan = new Product("Pan", "Panaderia", 3500f, 3, 0, null);
        pan.setId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(agua));
        when(productRepository.findById(2L)).thenReturn(Optional.of(pan));
        when(ventaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        VentaManualRequest request = new VentaManualRequest();
        request.setFormaPago("EFECTIVO");
        request.setItems(List.of(
                new VentaItemRequest(1L, 2),
                new VentaItemRequest(2L, 1)
        ));

        ResponseEntity<Object> response = ventaService.registrarVentaManual(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(3, agua.getStock());
        assertEquals(2, pan.getStock());
        assertEquals(8500f, ((Number) ((Map<String, Object>) response.getBody()).get("total")).floatValue(), 0.01f);
        verify(ventaRepository).saveAll(anyList());
    }
}
