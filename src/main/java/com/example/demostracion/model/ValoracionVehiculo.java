package com.example.demostracion.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "valoracion_vehiculo", uniqueConstraints = @UniqueConstraint(columnNames = "vehiculo_id"))
public class ValoracionVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion_vehiculo")
    private Long idValoracionVehiculo;

    @OneToOne
    @JoinColumn(name = "vehiculo_id", nullable = false, unique = true)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "inventario_id")
    private Inventario inventario;

    @Column(name = "soat_vencimiento")
    private LocalDate soatVencimiento;

    @Column(name = "tecnicomecanica_vencimiento")
    private LocalDate tecnicomecanicaVencimiento;

    @Column(name = "tarjeta_propiedad_ok", nullable = false)
    private boolean tarjetaPropiedadOk;

    @Column(name = "impuestos_al_dia", nullable = false)
    private boolean impuestosAlDia;

    @Column(name = "prenda_activa", nullable = false)
    private boolean prendaActiva;

    @Column(name = "precio_objetivo_manual")
    private Double precioObjetivoManual;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getIdValoracionVehiculo() {
        return idValoracionVehiculo;
    }

    public void setIdValoracionVehiculo(Long idValoracionVehiculo) {
        this.idValoracionVehiculo = idValoracionVehiculo;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public LocalDate getSoatVencimiento() {
        return soatVencimiento;
    }

    public void setSoatVencimiento(LocalDate soatVencimiento) {
        this.soatVencimiento = soatVencimiento;
    }

    public LocalDate getTecnicomecanicaVencimiento() {
        return tecnicomecanicaVencimiento;
    }

    public void setTecnicomecanicaVencimiento(LocalDate tecnicomecanicaVencimiento) {
        this.tecnicomecanicaVencimiento = tecnicomecanicaVencimiento;
    }

    public boolean isTarjetaPropiedadOk() {
        return tarjetaPropiedadOk;
    }

    public void setTarjetaPropiedadOk(boolean tarjetaPropiedadOk) {
        this.tarjetaPropiedadOk = tarjetaPropiedadOk;
    }

    public boolean isImpuestosAlDia() {
        return impuestosAlDia;
    }

    public void setImpuestosAlDia(boolean impuestosAlDia) {
        this.impuestosAlDia = impuestosAlDia;
    }

    public boolean isPrendaActiva() {
        return prendaActiva;
    }

    public void setPrendaActiva(boolean prendaActiva) {
        this.prendaActiva = prendaActiva;
    }

    public Double getPrecioObjetivoManual() {
        return precioObjetivoManual;
    }

    public void setPrecioObjetivoManual(Double precioObjetivoManual) {
        this.precioObjetivoManual = precioObjetivoManual;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}