package com.tomorrow.Negocio.Pedidos;

import com.tomorrow.Negocio.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "pedido_items")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Float precioUnitario;

    public PedidoItem() {
    }

    public PedidoItem(Product product, Integer cantidad, Float precioUnitario) {
        this.product = product;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // --- GETTERS Y SETTERS (Esenciales para que el Service no marque rojo) ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getCantidad() {
        return cantidad;
    }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Float getPrecioUnitario() {
        return precioUnitario;
    }
    public void setPrecioUnitario(Float precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    @Transient
    public Float getSubtotal() {
        return cantidad * precioUnitario;
    }
}