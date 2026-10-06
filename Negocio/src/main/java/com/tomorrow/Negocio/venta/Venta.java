package com.tomorrow.Negocio.venta;

import com.tomorrow.Negocio.product.Product;
import com.tomorrow.Negocio.Pedidos.Pedido;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int cantidad;

    private float precioUnitario;

    private LocalDateTime fecha;

    @Column(length = 30)
    private String formaPago = "EFECTIVO";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    public Venta() {
    }

    public Venta(Product product, int cantidad, float precioUnitario, LocalDateTime fecha) {
        this.product = product;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.fecha = fecha;
    }

    public Venta(Product product, int cantidad, float precioUnitario, LocalDateTime fecha,
                 String formaPago, Pedido pedido) {
        this(product, cantidad, precioUnitario, fecha);
        this.formaPago = formaPago;
        this.pedido = pedido;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public float getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(float precioUnitario) { this.precioUnitario = precioUnitario; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    @Transient
    public float getTotal() {
        return cantidad * precioUnitario;
    }
}