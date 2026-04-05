package com.example.demostracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdInventario")
    private Long idInventario;

    @Column(name = "UbicacionActual", length = 100)
    private String ubicacionActual;

    @Column(name = "Chasis", length = 255)
    private String chasis;

    @Column(name = "Marca", length = 255)
    private String marca;

    @Column(name = "Modelo", length = 255)
    private String modelo;

    @Column(name = "Anio")
    private Integer anio;

    @Column(name = "Color", length = 255)
    private String color;

    @Column(name = "Motor", length = 255)
    private String motor;

    @Column(name = "EstadoLogistico", length = 100)
    private String estadoLogistico;

    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible = 1;

    @Column(nullable = false)
    private boolean activo = true;

    // BD externa sin FK inventario->vehiculo: se mantiene transitorio para compatibilidad.
    @Transient
    private Vehiculo vehiculo;

    // ===== CONSTRUCTOR =====
    public Inventario() {}

    // ===== GETTERS & SETTERS =====

    public Long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(Long idInventario) {
        this.idInventario = idInventario;
    }

    public String getChasis() {
        return chasis;
    }

    public void setChasis(String chasis) {
        this.chasis = chasis;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMotor() {
        return motor;
    }

    public void setMotor(String motor) {
        this.motor = motor;
    }

    public String getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(String ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public String getEstadoLogistico() {
        return estadoLogistico;
    }

    public void setEstadoLogistico(String estadoLogistico) {
        this.estadoLogistico = estadoLogistico;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible == null || cantidadDisponible < 0 ? 0 : cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible == null ? 1 : cantidadDisponible;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
}