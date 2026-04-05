package com.example.demostracion.dto;

import java.time.LocalDate;

public class ValoracionVehiculoResumenDTO {

    private Long vehiculoId;
    private Long inventarioId;
    private String chasis;
    private String vehiculo;
    private String estadoPublicacion;
    private String estadoPublicacionEtiqueta;
    private String estadoDocumental;
    private String estadoDocumentalClase;
    private boolean listoParaPublicar;
    private Double precioActual;
    private Double precioObjetivoManual;
    private Double precioSugerido;
    private String accionPrecio;
    private int totalComparables;
    private LocalDate soatVencimiento;
    private LocalDate tecnicomecanicaVencimiento;

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
    }

    public String getChasis() {
        return chasis;
    }

    public void setChasis(String chasis) {
        this.chasis = chasis;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public void setEstadoPublicacion(String estadoPublicacion) {
        this.estadoPublicacion = estadoPublicacion;
    }

    public String getEstadoPublicacionEtiqueta() {
        return estadoPublicacionEtiqueta;
    }

    public void setEstadoPublicacionEtiqueta(String estadoPublicacionEtiqueta) {
        this.estadoPublicacionEtiqueta = estadoPublicacionEtiqueta;
    }

    public String getEstadoDocumental() {
        return estadoDocumental;
    }

    public void setEstadoDocumental(String estadoDocumental) {
        this.estadoDocumental = estadoDocumental;
    }

    public String getEstadoDocumentalClase() {
        return estadoDocumentalClase;
    }

    public void setEstadoDocumentalClase(String estadoDocumentalClase) {
        this.estadoDocumentalClase = estadoDocumentalClase;
    }

    public boolean isListoParaPublicar() {
        return listoParaPublicar;
    }

    public void setListoParaPublicar(boolean listoParaPublicar) {
        this.listoParaPublicar = listoParaPublicar;
    }

    public Double getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(Double precioActual) {
        this.precioActual = precioActual;
    }

    public Double getPrecioObjetivoManual() {
        return precioObjetivoManual;
    }

    public void setPrecioObjetivoManual(Double precioObjetivoManual) {
        this.precioObjetivoManual = precioObjetivoManual;
    }

    public Double getPrecioSugerido() {
        return precioSugerido;
    }

    public void setPrecioSugerido(Double precioSugerido) {
        this.precioSugerido = precioSugerido;
    }

    public String getAccionPrecio() {
        return accionPrecio;
    }

    public void setAccionPrecio(String accionPrecio) {
        this.accionPrecio = accionPrecio;
    }

    public int getTotalComparables() {
        return totalComparables;
    }

    public void setTotalComparables(int totalComparables) {
        this.totalComparables = totalComparables;
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
}