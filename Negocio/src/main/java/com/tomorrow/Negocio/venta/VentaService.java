package com.tomorrow.Negocio.venta;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.product.ProductRepository;
import com.tomorrow.Negocio.Pedidos.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductRepository productRepository;

    @Autowired
    public VentaService(VentaRepository ventaRepository, ProductRepository productRepository) {
        this.ventaRepository = ventaRepository;
        this.productRepository = productRepository;
    }

    public List<Venta> getVentas() {
        return ventaRepository.findAllByOrderByFechaDesc();
    }

    @Transactional
    public ResponseEntity<Object> registrarVenta(Long productId, int cantidad, String formaPago) {
        Map<String, Object> body = new HashMap<>();

        Optional<Product> productoOpt = productRepository.findById(productId);
        if (productoOpt.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "No existe ese producto");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Product product = productoOpt.get();
        if (product.getStock() < cantidad) {
            body.put("error", true);
            body.put("mensaje", "No hay suficiente stock (disponible: " + product.getStock() + ")");
            return new ResponseEntity<>(body, HttpStatus.CONFLICT);
        }

        product.setStock(product.getStock() - cantidad);
        productRepository.save(product);

        Venta venta = new Venta(product, cantidad, product.getPrice(), LocalDateTime.now(), formaPago, null);
        Venta guardada = ventaRepository.save(venta);

        body.put("mensaje", "Venta registrada correctamente");
        body.put("data", guardada);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<Object> registrarVentaManual(VentaManualRequest request) {
        Map<String, Object> body = new HashMap<>();
        List<Venta> ventas = new ArrayList<>();
        List<Product> productosActualizados = new ArrayList<>();
        float total = 0f;

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "Debés cargar al menos un producto para la venta");
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        for (VentaItemRequest itemRequest : request.getItems()) {
            Optional<Product> productoOpt = productRepository.findById(itemRequest.getProductId());
            if (productoOpt.isEmpty()) {
                body.put("error", true);
                body.put("mensaje", "No existe el producto con ID " + itemRequest.getProductId());
                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
            }

            Product product = productoOpt.get();
            if (product.getStock() < itemRequest.getCantidad()) {
                body.put("error", true);
                body.put("mensaje", "No hay suficiente stock para " + product.getName() + " (disponible: " + product.getStock() + ")");
                return new ResponseEntity<>(body, HttpStatus.CONFLICT);
            }

            total += product.getPrice() * itemRequest.getCantidad();
            product.setStock(product.getStock() - itemRequest.getCantidad());
            productosActualizados.add(product);

            ventas.add(new Venta(product, itemRequest.getCantidad(), product.getPrice(), LocalDateTime.now(), request.getFormaPago(), null));
        }

        for (Product product : productosActualizados) {
            productRepository.save(product);
        }

        List<Venta> guardadas = ventaRepository.saveAll(ventas);
        body.put("mensaje", "Venta manual registrada correctamente");
        body.put("total", total);
        body.put("ventas", guardadas);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    public byte[] generarPdfVentaManual(VentaManualRequest request) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        float total = 0f;

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            document.add(new Paragraph("Despensa Aurora"));
            document.add(new Paragraph("Ticket de venta manual"));
            document.add(new Paragraph("Fecha: " + LocalDateTime.now()));
            document.add(new Paragraph("Forma de pago: " + request.getFormaPago()));
            document.add(new Paragraph(" "));

            for (VentaItemRequest item : request.getItems()) {
                Optional<Product> productoOpt = productRepository.findById(item.getProductId());
                if (productoOpt.isEmpty()) {
                    continue;
                }
                Product product = productoOpt.get();
                float subtotal = product.getPrice() * item.getCantidad();
                total += subtotal;
                document.add(new Paragraph(
                        product.getName() + " x " + item.getCantidad() + " - Gs. " + subtotal
                ));
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("TOTAL: Gs. " + total));
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF de la venta", e);
        }
    }

    @Transactional
    public List<Venta> registrarPedidoEntregado(Pedido pedido, String formaPago) {
        LocalDateTime fechaVenta = LocalDateTime.now();
        List<Venta> ventas = pedido.getItems().stream()
                .map(item -> new Venta(item.getProduct(), item.getCantidad(), item.getPrecioUnitario(),
                        fechaVenta, formaPago, pedido))
                .toList();
        return ventaRepository.saveAll(ventas);
    }
}