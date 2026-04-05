package com.example.demostracion.dto;

public class RadarPrecioDTO {

    private int comparablesActivos;
    private int comparablesHistoricos;
    private double precioPromedioActivo;
    private double precioPromedioHistorico;
    private double precioSugerido;
    private double rangoMinimo;
    private double rangoMaximo;
    private String confianza;
    private String accionSugerida;
    private double desviacionPorcentual;
    private int comparablesMercado;
    private double precioPromedioMercado;
    private boolean mercadoIntegrado;
    private String fuentesMercado;
    private String origenAnalisis;

    public int getComparablesActivos() {
        return comparablesActivos;
    }

    public void setComparablesActivos(int comparablesActivos) {
        this.comparablesActivos = comparablesActivos;
    }

    public int getComparablesHistoricos() {
        return comparablesHistoricos;
    }

    public void setComparablesHistoricos(int comparablesHistoricos) {
        this.comparablesHistoricos = comparablesHistoricos;
    }

    public double getPrecioPromedioActivo() {
        return precioPromedioActivo;
    }

    public void setPrecioPromedioActivo(double precioPromedioActivo) {
        this.precioPromedioActivo = precioPromedioActivo;
    }

    public double getPrecioPromedioHistorico() {
        return precioPromedioHistorico;
    }

    public void setPrecioPromedioHistorico(double precioPromedioHistorico) {
        this.precioPromedioHistorico = precioPromedioHistorico;
    }

    public double getPrecioSugerido() {
        return precioSugerido;
    }

    public void setPrecioSugerido(double precioSugerido) {
        this.precioSugerido = precioSugerido;
    }

    public double getRangoMinimo() {
        return rangoMinimo;
    }

    public void setRangoMinimo(double rangoMinimo) {
        this.rangoMinimo = rangoMinimo;
    }

    public double getRangoMaximo() {
        return rangoMaximo;
    }

    public void setRangoMaximo(double rangoMaximo) {
        this.rangoMaximo = rangoMaximo;
    }

    public String getConfianza() {
        return confianza;
    }

    public void setConfianza(String confianza) {
        this.confianza = confianza;
    }

    public String getAccionSugerida() {
        return accionSugerida;
    }

    public void setAccionSugerida(String accionSugerida) {
        this.accionSugerida = accionSugerida;
    }

    public double getDesviacionPorcentual() {
        return desviacionPorcentual;
    }

    public void setDesviacionPorcentual(double desviacionPorcentual) {
        this.desviacionPorcentual = desviacionPorcentual;
    }

    public int getComparablesMercado() {
        return comparablesMercado;
    }

    public void setComparablesMercado(int comparablesMercado) {
        this.comparablesMercado = comparablesMercado;
    }

    public double getPrecioPromedioMercado() {
        return precioPromedioMercado;
    }

    public void setPrecioPromedioMercado(double precioPromedioMercado) {
        this.precioPromedioMercado = precioPromedioMercado;
    }

    public boolean isMercadoIntegrado() {
        return mercadoIntegrado;
    }

    public void setMercadoIntegrado(boolean mercadoIntegrado) {
        this.mercadoIntegrado = mercadoIntegrado;
    }

    public String getFuentesMercado() {
        return fuentesMercado;
    }

    public void setFuentesMercado(String fuentesMercado) {
        this.fuentesMercado = fuentesMercado;
    }

    public String getOrigenAnalisis() {
        return origenAnalisis;
    }

    public void setOrigenAnalisis(String origenAnalisis) {
        this.origenAnalisis = origenAnalisis;
    }
}