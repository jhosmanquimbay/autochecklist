package com.example.demostracion.dto;

import java.time.LocalDateTime;

public class DetalleContableVentaDTO {

    private Long pedidoId;
    private Long idContabilidadVenta;
    private LocalDateTime fechaOperacion;
    private String estado;
    private String estadoEtiqueta;
    private String cliente;
    private String vendedor;
    private String vehiculo;
    private String chasis;
    private boolean configurado;
    private Double precioPublicado;
    private Double precioVentaFinal;
    private Double costoTotal;
    private Double utilidadBruta;
    private Double comision;
    private Double utilidadNeta;
    private Double reinversion;
    private Double margen;

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getIdContabilidadVenta() {
        return idContabilidadVenta;
    }

    public void setIdContabilidadVenta(Long idContabilidadVenta) {
        this.idContabilidadVenta = idContabilidadVenta;
    }

    public LocalDateTime getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(LocalDateTime fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoEtiqueta() {
        return estadoEtiqueta;
    }

    public void setEstadoEtiqueta(String estadoEtiqueta) {
        this.estadoEtiqueta = estadoEtiqueta;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getChasis() {
        return chasis;
    }

    public void setChasis(String chasis) {
        this.chasis = chasis;
    }

    public boolean isConfigurado() {
        return configurado;
    }

    public void setConfigurado(boolean configurado) {
        this.configurado = configurado;
    }

    public Double getPrecioPublicado() {
        return precioPublicado;
    }

    public void setPrecioPublicado(Double precioPublicado) {
        this.precioPublicado = precioPublicado;
    }

    public Double getPrecioVentaFinal() {
        return precioVentaFinal;
    }

    public void setPrecioVentaFinal(Double precioVentaFinal) {
        this.precioVentaFinal = precioVentaFinal;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Double getUtilidadBruta() {
        return utilidadBruta;
    }

    public void setUtilidadBruta(Double utilidadBruta) {
        this.utilidadBruta = utilidadBruta;
    }

    public Double getComision() {
        return comision;
    }

    public void setComision(Double comision) {
        this.comision = comision;
    }

    public Double getUtilidadNeta() {
        return utilidadNeta;
    }

    public void setUtilidadNeta(Double utilidadNeta) {
        this.utilidadNeta = utilidadNeta;
    }

    public Double getReinversion() {
        return reinversion;
    }

    public void setReinversion(Double reinversion) {
        this.reinversion = reinversion;
    }

    public Double getMargen() {
        return margen;
    }

    public void setMargen(Double margen) {
        this.margen = margen;
    }

    public boolean isRentable() {
        return utilidadNeta != null && utilidadNeta > 0;
    }

    public boolean isPerdida() {
        return utilidadNeta != null && utilidadNeta < 0;
    }
}