package com.example.demostracion.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class SeguimientoFinanciamientoForm {

    private Long solicitudId;
    private Boolean crearNegocio;
    private String estadoDocumental;
    private String etapaProceso;
    private String entidadFinanciera;
    private Double montoDesembolsado;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesembolsoProgramada;

    private String observacionesGestion;

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public Boolean getCrearNegocio() {
        return crearNegocio;
    }

    public void setCrearNegocio(Boolean crearNegocio) {
        this.crearNegocio = crearNegocio;
    }

    public String getEstadoDocumental() {
        return estadoDocumental;
    }

    public void setEstadoDocumental(String estadoDocumental) {
        this.estadoDocumental = estadoDocumental;
    }

    public String getEtapaProceso() {
        return etapaProceso;
    }

    public void setEtapaProceso(String etapaProceso) {
        this.etapaProceso = etapaProceso;
    }

    public String getEntidadFinanciera() {
        return entidadFinanciera;
    }

    public void setEntidadFinanciera(String entidadFinanciera) {
        this.entidadFinanciera = entidadFinanciera;
    }

    public Double getMontoDesembolsado() {
        return montoDesembolsado;
    }

    public void setMontoDesembolsado(Double montoDesembolsado) {
        this.montoDesembolsado = montoDesembolsado;
    }

    public LocalDate getFechaDesembolsoProgramada() {
        return fechaDesembolsoProgramada;
    }

    public void setFechaDesembolsoProgramada(LocalDate fechaDesembolsoProgramada) {
        this.fechaDesembolsoProgramada = fechaDesembolsoProgramada;
    }

    public String getObservacionesGestion() {
        return observacionesGestion;
    }

    public void setObservacionesGestion(String observacionesGestion) {
        this.observacionesGestion = observacionesGestion;
    }
}