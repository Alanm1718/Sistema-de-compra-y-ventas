package com.tomorrow.Negocio.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProducts() {
        return this.productRepository.findAll();
    }

    public List<Product> getProductosConStockBajo() {
        return this.productRepository.findProductosConStockBajo();
    }

    public ResponseEntity<Object> createProduct(Product product) {
        Map<String, Object> body = new HashMap<>();

        if (productRepository.findByName(product.getName()).isPresent()) {
            body.put("error", true);
            body.put("mensaje", "Ya existe un producto con ese nombre");
            return new ResponseEntity<>(body, HttpStatus.CONFLICT);
        }

        product.setId(null);
        Product guardado = productRepository.save(product);

        body.put("mensaje", "Se guardó correctamente");
        body.put("data", guardado);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateProduct(Long id, Product product) {
        Map<String, Object> body = new HashMap<>();

        Optional<Product> existente = productRepository.findById(id);
        if (existente.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "No existe un producto con ese id");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Optional<Product> mismoNombre = productRepository.findByName(product.getName());
        if (mismoNombre.isPresent() && !mismoNombre.get().getId().equals(id)) {
            body.put("error", true);
            body.put("mensaje", "Ya existe otro producto con ese nombre");
            return new ResponseEntity<>(body, HttpStatus.CONFLICT);
        }

        product.setId(id);
        Product actualizado = productRepository.save(product);

        body.put("mensaje", "Se actualizó correctamente");
        body.put("data", actualizado);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<Object> ajustarStock(Long id, int cantidad) {
        Map<String, Object> body = new HashMap<>();

        Optional<Product> existente = productRepository.findById(id);
        if (existente.isEmpty()) {
            body.put("error", true);
            body.put("mensaje", "No existe un producto con ese id");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        Product product = existente.get();
        int nuevoStock = product.getStock() + cantidad;

        if (nuevoStock < 0) {
            body.put("error", true);
            body.put("mensaje", "No hay suficiente stock para restar esa cantidad");
            return new ResponseEntity<>(body, HttpStatus.CONFLICT);
        }

        product.setStock(nuevoStock);
        Product actualizado = productRepository.save(product);

        body.put("mensaje", cantidad >= 0 ? "Stock incrementado" : "Stock descontado");
        body.put("data", actualizado);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    public ResponseEntity<Object> deleteProduct(Long id) {
        Map<String, Object> body = new HashMap<>();

        if (!productRepository.existsById(id)) {
            body.put("error", true);
            body.put("mensaje", "No existe este producto");
            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        productRepository.deleteById(id);
        body.put("mensaje", "Producto eliminado");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }
}