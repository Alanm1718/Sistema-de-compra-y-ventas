package com.tomorrow.Negocio.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @Positive(message = "El precio debe ser mayor a 0")
    private float price;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private int stock;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private int stockMinimo;

    private LocalDate fecha;

    public Product() {
    }

    public Product(Long id, String name, String categoria, float price, int stock, int stockMinimo, LocalDate fecha) {
        this.id = id;
        this.name = name;
        this.categoria = categoria;
        this.price = price;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.fecha = fecha;
    }

    public Product(String name, String categoria, float price, int stock, int stockMinimo, LocalDate fecha) {
        this.name = name;
        this.categoria = categoria;
        this.price = price;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    @Transient
    public boolean isStockBajo() {
        return stock <= stockMinimo;
    }
}