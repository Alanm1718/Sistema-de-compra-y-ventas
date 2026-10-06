package com.tomorrow.Negocio.venta;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/ventas")
public class VentaController {

    private final VentaService ventaService;

    @Autowired
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public List<Venta> getVentas() {
        return ventaService.getVentas();
    }

    @PostMapping
    public ResponseEntity<Object> registrarVenta(@Valid @RequestBody VentaRequest request) {
        return ventaService.registrarVenta(request.getProductId(), request.getCantidad(), request.getFormaPago());
    }

    @PostMapping(path = "/manual")
    public ResponseEntity<Object> registrarVentaManual(@Valid @RequestBody VentaManualRequest request) {
        return ventaService.registrarVentaManual(request);
    }

    @PostMapping(path = "/manual/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarVentaManualPdf(@Valid @RequestBody VentaManualRequest request) {
        byte[] pdf = ventaService.generarPdfVentaManual(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "venta-manual.pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}