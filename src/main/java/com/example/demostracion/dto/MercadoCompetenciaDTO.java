package com.example.demostracion.dto;

import java.util.List;

public class MercadoCompetenciaDTO {

    private boolean disponible;
    private String mensaje;
    private String fuentes;
    private int totalMuestras;
    private double precioPromedio;
    private double precioMinimo;
    private double precioMaximo;
    private List<CompetidorPrecioDTO> comparables = List.of();

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFuentes() {
        return fuentes;
    }

    public void setFuentes(String fuentes) {
        this.fuentes = fuentes;
    }

    public int getTotalMuestras() {
        return totalMuestras;
    }

    public void setTotalMuestras(int totalMuestras) {
        this.totalMuestras = totalMuestras;
    }

    public double getPrecioPromedio() {
        return precioPromedio;
    }

    public void setPrecioPromedio(double precioPromedio) {
        this.precioPromedio = precioPromedio;
    }

    public double getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(double precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public double getPrecioMaximo() {
        return precioMaximo;
    }

    public void setPrecioMaximo(double precioMaximo) {
        this.precioMaximo = precioMaximo;
    }

    public List<CompetidorPrecioDTO> getComparables() {
        return comparables;
    }

    public void setComparables(List<CompetidorPrecioDTO> comparables) {
        this.comparables = comparables;
    }
}