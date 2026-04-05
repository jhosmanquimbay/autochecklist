package com.example.demostracion.dto;

import java.util.ArrayList;
import java.util.List;

public class FinanciamientoResultadoDTO {

    private Long vehiculoId;
    private String nombreVehiculo;
    private double precioVehiculo;
    private List<Integer> plazosDisponibles = new ArrayList<>();
    private double porcentajeCuotaInicialMinima;
    private double cuotaInicialMinima;
    private double cuotaInicial;
    private double porcentajeCuotaInicial;
    private int plazoMeses;
    private double montoFinanciar;
    private double montoMaximoSugerido;
    private double tasaEfectivaAnual;
    private double tasaMesVencida;
    private double cuotaCapitalInteres;
    private double segurosMensuales;
    private double cargosMensuales;
    private double cuotaMensualTotal;
    private double ingresoMinimoSugerido;
    private double capacidadPagoDisponible;
    private double relacionCuotaIngreso;
    private double relacionEndeudamientoTotal;
    private String estadoAnalisis;
    private boolean aprobadoPreliminar;
    private String mensajeDecision;
    private String canalOrigen;
    private List<String> documentosRequeridos = new ArrayList<>();

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getNombreVehiculo() {
        return nombreVehiculo;
    }

    public void setNombreVehiculo(String nombreVehiculo) {
        this.nombreVehiculo = nombreVehiculo;
    }

    public double getPrecioVehiculo() {
        return precioVehiculo;
    }

    public void setPrecioVehiculo(double precioVehiculo) {
        this.precioVehiculo = precioVehiculo;
    }

    public List<Integer> getPlazosDisponibles() {
        return plazosDisponibles;
    }

    public void setPlazosDisponibles(List<Integer> plazosDisponibles) {
        this.plazosDisponibles = plazosDisponibles;
    }

    public double getPorcentajeCuotaInicialMinima() {
        return porcentajeCuotaInicialMinima;
    }

    public void setPorcentajeCuotaInicialMinima(double porcentajeCuotaInicialMinima) {
        this.porcentajeCuotaInicialMinima = porcentajeCuotaInicialMinima;
    }

    public double getCuotaInicialMinima() {
        return cuotaInicialMinima;
    }

    public void setCuotaInicialMinima(double cuotaInicialMinima) {
        this.cuotaInicialMinima = cuotaInicialMinima;
    }

    public double getCuotaInicial() {
        return cuotaInicial;
    }

    public void setCuotaInicial(double cuotaInicial) {
        this.cuotaInicial = cuotaInicial;
    }

    public double getPorcentajeCuotaInicial() {
        return porcentajeCuotaInicial;
    }

    public void setPorcentajeCuotaInicial(double porcentajeCuotaInicial) {
        this.porcentajeCuotaInicial = porcentajeCuotaInicial;
    }

    public int getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(int plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public double getMontoFinanciar() {
        return montoFinanciar;
    }

    public void setMontoFinanciar(double montoFinanciar) {
        this.montoFinanciar = montoFinanciar;
    }

    public double getMontoMaximoSugerido() {
        return montoMaximoSugerido;
    }

    public void setMontoMaximoSugerido(double montoMaximoSugerido) {
        this.montoMaximoSugerido = montoMaximoSugerido;
    }

    public double getTasaEfectivaAnual() {
        return tasaEfectivaAnual;
    }

    public void setTasaEfectivaAnual(double tasaEfectivaAnual) {
        this.tasaEfectivaAnual = tasaEfectivaAnual;
    }

    public double getTasaMesVencida() {
        return tasaMesVencida;
    }

    public void setTasaMesVencida(double tasaMesVencida) {
        this.tasaMesVencida = tasaMesVencida;
    }

    public double getCuotaCapitalInteres() {
        return cuotaCapitalInteres;
    }

    public void setCuotaCapitalInteres(double cuotaCapitalInteres) {
        this.cuotaCapitalInteres = cuotaCapitalInteres;
    }

    public double getSegurosMensuales() {
        return segurosMensuales;
    }

    public void setSegurosMensuales(double segurosMensuales) {
        this.segurosMensuales = segurosMensuales;
    }

    public double getCargosMensuales() {
        return cargosMensuales;
    }

    public void setCargosMensuales(double cargosMensuales) {
        this.cargosMensuales = cargosMensuales;
    }

    public double getCuotaMensualTotal() {
        return cuotaMensualTotal;
    }

    public void setCuotaMensualTotal(double cuotaMensualTotal) {
        this.cuotaMensualTotal = cuotaMensualTotal;
    }

    public double getIngresoMinimoSugerido() {
        return ingresoMinimoSugerido;
    }

    public void setIngresoMinimoSugerido(double ingresoMinimoSugerido) {
        this.ingresoMinimoSugerido = ingresoMinimoSugerido;
    }

    public double getCapacidadPagoDisponible() {
        return capacidadPagoDisponible;
    }

    public void setCapacidadPagoDisponible(double capacidadPagoDisponible) {
        this.capacidadPagoDisponible = capacidadPagoDisponible;
    }

    public double getRelacionCuotaIngreso() {
        return relacionCuotaIngreso;
    }

    public void setRelacionCuotaIngreso(double relacionCuotaIngreso) {
        this.relacionCuotaIngreso = relacionCuotaIngreso;
    }

    public double getRelacionEndeudamientoTotal() {
        return relacionEndeudamientoTotal;
    }

    public void setRelacionEndeudamientoTotal(double relacionEndeudamientoTotal) {
        this.relacionEndeudamientoTotal = relacionEndeudamientoTotal;
    }

    public String getEstadoAnalisis() {
        return estadoAnalisis;
    }

    public void setEstadoAnalisis(String estadoAnalisis) {
        this.estadoAnalisis = estadoAnalisis;
    }

    public boolean isAprobadoPreliminar() {
        return aprobadoPreliminar;
    }

    public void setAprobadoPreliminar(boolean aprobadoPreliminar) {
        this.aprobadoPreliminar = aprobadoPreliminar;
    }

    public String getMensajeDecision() {
        return mensajeDecision;
    }

    public void setMensajeDecision(String mensajeDecision) {
        this.mensajeDecision = mensajeDecision;
    }

    public String getCanalOrigen() {
        return canalOrigen;
    }

    public void setCanalOrigen(String canalOrigen) {
        this.canalOrigen = canalOrigen;
    }

    public List<String> getDocumentosRequeridos() {
        return documentosRequeridos;
    }

    public void setDocumentosRequeridos(List<String> documentosRequeridos) {
        this.documentosRequeridos = documentosRequeridos;
    }

    public String getEstadoAnalisisEtiqueta() {
        return switch (estadoAnalisis == null ? "" : estadoAnalisis.trim().toUpperCase()) {
            case "PREAPROBADO" -> "Preaprobado";
            case "EN_ESTUDIO" -> "En estudio";
            case "NO_VIABLE" -> "No viable";
            default -> "Estimación base";
        };
    }
}