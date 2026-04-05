package com.example.demostracion.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VehiculoClienteDTO {

    private final Long idVehiculo;
    private final String marca;
    private final String modelo;
    private final Integer anio;
    private final Double precio;
    private final String descripcion;
    private final String especificacionesTecnicas;
    private final String tipoCombustible;
    private final String transmision;
    private final String cilindrada;
    private final Integer stockDisponible;
    private final boolean disponible;
    private final Integer popularidad;
    private final LocalDateTime fechaCreacion;
    private final List<String> imagenes;
    private final Double cuotaMensualDesde;
    private final Integer plazoFinanciamientoReferencia;
    private final Double porcentajeCuotaInicialMinima;

    public VehiculoClienteDTO(Long idVehiculo,
                              String marca,
                              String modelo,
                              Integer anio,
                              Double precio,
                              String descripcion,
                              String especificacionesTecnicas,
                              String tipoCombustible,
                              String transmision,
                              String cilindrada,
                              Integer stockDisponible,
                              boolean disponible,
                              Integer popularidad,
                              LocalDateTime fechaCreacion,
                              List<String> imagenes,
                              Double cuotaMensualDesde,
                              Integer plazoFinanciamientoReferencia,
                              Double porcentajeCuotaInicialMinima) {
        this.idVehiculo = idVehiculo;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.precio = precio;
        this.descripcion = descripcion;
        this.especificacionesTecnicas = especificacionesTecnicas;
        this.tipoCombustible = tipoCombustible;
        this.transmision = transmision;
        this.cilindrada = cilindrada;
        this.stockDisponible = stockDisponible;
        this.disponible = disponible;
        this.popularidad = popularidad;
        this.fechaCreacion = fechaCreacion;
        this.imagenes = imagenes;
        this.cuotaMensualDesde = cuotaMensualDesde;
        this.plazoFinanciamientoReferencia = plazoFinanciamientoReferencia;
        this.porcentajeCuotaInicialMinima = porcentajeCuotaInicialMinima;
    }

    public Long getIdVehiculo() {
        return idVehiculo;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public Double getPrecio() {
        return precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEspecificacionesTecnicas() {
        return especificacionesTecnicas;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }

    public String getTransmision() {
        return transmision;
    }

    public String getCilindrada() {
        return cilindrada;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public Integer getPopularidad() {
        return popularidad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public Double getCuotaMensualDesde() {
        return cuotaMensualDesde;
    }

    public Integer getPlazoFinanciamientoReferencia() {
        return plazoFinanciamientoReferencia;
    }

    public Double getPorcentajeCuotaInicialMinima() {
        return porcentajeCuotaInicialMinima;
    }

    public String getNombreCompleto() {
        String marcaActual = marca == null ? "Vehículo" : marca;
        String modeloActual = modelo == null ? "sin modelo" : modelo;
        return marcaActual + " " + modeloActual;
    }

    public String getTextoStock() {
        if (stockDisponible == null || stockDisponible <= 0) {
            return "Disponible por pedido";
        }
        return stockDisponible == 1 ? "1 unidad disponible" : stockDisponible + " unidades disponibles";
    }
}