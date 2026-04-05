package com.example.demostracion.dto;

public class ResumenContableDTO {

    private int totalOperaciones;
    private int operacionesLiquidadas;
    private long pendientesLiquidacion;
    private double ingresosLiquidados;
    private double costosLiquidados;
    private double utilidadBruta;
    private double comisiones;
    private double utilidadNeta;
    private double reinversionSugerida;
    private double margenPromedio;
    private double ticketPromedio;

    public int getTotalOperaciones() {
        return totalOperaciones;
    }

    public void setTotalOperaciones(int totalOperaciones) {
        this.totalOperaciones = totalOperaciones;
    }

    public int getOperacionesLiquidadas() {
        return operacionesLiquidadas;
    }

    public void setOperacionesLiquidadas(int operacionesLiquidadas) {
        this.operacionesLiquidadas = operacionesLiquidadas;
    }

    public long getPendientesLiquidacion() {
        return pendientesLiquidacion;
    }

    public void setPendientesLiquidacion(long pendientesLiquidacion) {
        this.pendientesLiquidacion = pendientesLiquidacion;
    }

    public double getIngresosLiquidados() {
        return ingresosLiquidados;
    }

    public void setIngresosLiquidados(double ingresosLiquidados) {
        this.ingresosLiquidados = ingresosLiquidados;
    }

    public double getCostosLiquidados() {
        return costosLiquidados;
    }

    public void setCostosLiquidados(double costosLiquidados) {
        this.costosLiquidados = costosLiquidados;
    }

    public double getUtilidadBruta() {
        return utilidadBruta;
    }

    public void setUtilidadBruta(double utilidadBruta) {
        this.utilidadBruta = utilidadBruta;
    }

    public double getComisiones() {
        return comisiones;
    }

    public void setComisiones(double comisiones) {
        this.comisiones = comisiones;
    }

    public double getUtilidadNeta() {
        return utilidadNeta;
    }

    public void setUtilidadNeta(double utilidadNeta) {
        this.utilidadNeta = utilidadNeta;
    }

    public double getReinversionSugerida() {
        return reinversionSugerida;
    }

    public void setReinversionSugerida(double reinversionSugerida) {
        this.reinversionSugerida = reinversionSugerida;
    }

    public double getMargenPromedio() {
        return margenPromedio;
    }

    public void setMargenPromedio(double margenPromedio) {
        this.margenPromedio = margenPromedio;
    }

    public double getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(double ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }
}