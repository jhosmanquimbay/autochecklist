package com.example.demostracion.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    private String estado = "pendiente"; // pendiente, en_transito, entregado, rechazado

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "conductor_id", nullable = true)
    private Conductor conductor;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = true)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "cliente_usuario_id", nullable = true)
    private Usuario clienteUsuario;

    @Column(name = "stock_descontado", nullable = false)
    private boolean stockDescontado = false;

    // Getters y Setters
    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Conductor getConductor() { return conductor; }
    public void setConductor(Conductor conductor) { this.conductor = conductor; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Usuario getClienteUsuario() { return clienteUsuario; }
    public void setClienteUsuario(Usuario clienteUsuario) { this.clienteUsuario = clienteUsuario; }

    public boolean isStockDescontado() { return stockDescontado; }
    public void setStockDescontado(boolean stockDescontado) { this.stockDescontado = stockDescontado; }
}
    