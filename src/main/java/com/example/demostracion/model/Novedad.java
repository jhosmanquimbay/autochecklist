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
@Table(name = "novedades")
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNovedades")
    private Long idNovedades;

    @Column(name = "TipoNovedad", nullable = false, length = 100)
    private String tipoNovedad;

    @Column(name = "Descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "VehiculoChasis", length = 255)
    private String vehiculoChasis;

    @Column(name = "VehiculoReferencia", length = 255)
    private String vehiculoReferencia;

    @Column(name = "OrigenReporte", length = 100)
    private String origenReporte;

    @Column(name = "Prioridad", length = 50)
    private String prioridad;

    @Column(name = "AplicaGarantia")
    private Boolean aplicaGarantia;

    @Column(name = "AccionRequerida", columnDefinition = "TEXT")
    private String accionRequerida;

    @Column(name = "ObservacionGerente", columnDefinition = "TEXT")
    private String observacionGerente;

    @Column(name = "FechaReporte")
    private LocalDateTime fechaReporte;

    @Column(name = "FechaGestion")
    private LocalDateTime fechaGestion;

    @Column(name = "Evidencia")
    private String evidencia; // Aquí solo guardamos el nombre del archivo

    @Column(name = "Estado", nullable = false, length = 50)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "Id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    // ✅ Getters y Setters
    public Long getIdNovedades() {
        return idNovedades;
    }

    public void setIdNovedades(Long idNovedades) {
        this.idNovedades = idNovedades;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getVehiculoChasis() {
        return vehiculoChasis;
    }

    public void setVehiculoChasis(String vehiculoChasis) {
        this.vehiculoChasis = vehiculoChasis;
    }

    public String getVehiculoReferencia() {
        return vehiculoReferencia;
    }

    public void setVehiculoReferencia(String vehiculoReferencia) {
        this.vehiculoReferencia = vehiculoReferencia;
    }

    public String getOrigenReporte() {
        return origenReporte;
    }

    public void setOrigenReporte(String origenReporte) {
        this.origenReporte = origenReporte;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Boolean getAplicaGarantia() {
        return aplicaGarantia;
    }

    public void setAplicaGarantia(Boolean aplicaGarantia) {
        this.aplicaGarantia = aplicaGarantia;
    }

    public String getAccionRequerida() {
        return accionRequerida;
    }

    public void setAccionRequerida(String accionRequerida) {
        this.accionRequerida = accionRequerida;
    }

    public String getObservacionGerente() {
        return observacionGerente;
    }

    public void setObservacionGerente(String observacionGerente) {
        this.observacionGerente = observacionGerente;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDateTime fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public LocalDateTime getFechaGestion() {
        return fechaGestion;
    }

    public void setFechaGestion(LocalDateTime fechaGestion) {
        this.fechaGestion = fechaGestion;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
