package com.tomorrow.Negocio.product;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts() {
        return productService.getProducts();
    }

    @GetMapping(path = "/stock-bajo")
    public List<Product> getProductosConStockBajo() {
        return productService.getProductosConStockBajo();
    }

    @PostMapping
    public ResponseEntity<Object> crearProducto(@Valid @RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping(path = "{productId}")
    public ResponseEntity<Object> actualizarProducto(
            @PathVariable("productId") Long id,
            @Valid @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @PatchMapping(path = "{productId}/stock")
    public ResponseEntity<Object> ajustarStock(
            @PathVariable("productId") Long id,
            @RequestBody Map<String, Integer> payload) {
        int cantidad = payload.getOrDefault("cantidad", 0);
        return productService.ajustarStock(id, cantidad);
    }

    @DeleteMapping(path = "{productId}")
    public ResponseEntity<Object> eliminarProducto(@PathVariable("productId") Long id) {
        return productService.deleteProduct(id);
    }
}