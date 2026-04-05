package com.example.demostracion.model;

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
@Table(name = "contabilidad_venta", uniqueConstraints = @UniqueConstraint(columnNames = "pedido_id"))
public class ContabilidadVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contabilidad_venta")
    private Long idContabilidadVenta;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "inventario_id")
    private Inventario inventario;

    @Column(name = "precio_publicado_snapshot")
    private Double precioPublicadoSnapshot;

    @Column(name = "precio_venta_final")
    private Double precioVentaFinal;

    @Column(name = "costo_base")
    private Double costoBase;

    @Column(name = "costo_acondicionamiento")
    private Double costoAcondicionamiento;

    @Column(name = "costo_traslado")
    private Double costoTraslado;

    @Column(name = "costo_administrativo")
    private Double costoAdministrativo;

    @Column(name = "gasto_publicacion")
    private Double gastoPublicacion;

    @Column(name = "gastos_cierre")
    private Double gastosCierre;

    @Column(name = "porcentaje_comision")
    private Double porcentajeComision = 8.0;

    @Column(name = "porcentaje_reinversion")
    private Double porcentajeReinversion = 60.0;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void actualizarFechaActualizacion() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getIdContabilidadVenta() {
        return idContabilidadVenta;
    }

    public void setIdContabilidadVenta(Long idContabilidadVenta) {
        this.idContabilidadVenta = idContabilidadVenta;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
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

    public Double getPrecioPublicadoSnapshot() {
        return precioPublicadoSnapshot;
    }

    public void setPrecioPublicadoSnapshot(Double precioPublicadoSnapshot) {
        this.precioPublicadoSnapshot = precioPublicadoSnapshot;
    }

    public Double getPrecioVentaFinal() {
        return precioVentaFinal;
    }

    public void setPrecioVentaFinal(Double precioVentaFinal) {
        this.precioVentaFinal = precioVentaFinal;
    }

    public Double getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(Double costoBase) {
        this.costoBase = costoBase;
    }

    public Double getCostoAcondicionamiento() {
        return costoAcondicionamiento;
    }

    public void setCostoAcondicionamiento(Double costoAcondicionamiento) {
        this.costoAcondicionamiento = costoAcondicionamiento;
    }

    public Double getCostoTraslado() {
        return costoTraslado;
    }

    public void setCostoTraslado(Double costoTraslado) {
        this.costoTraslado = costoTraslado;
    }

    public Double getCostoAdministrativo() {
        return costoAdministrativo;
    }

    public void setCostoAdministrativo(Double costoAdministrativo) {
        this.costoAdministrativo = costoAdministrativo;
    }

    public Double getGastoPublicacion() {
        return gastoPublicacion;
    }

    public void setGastoPublicacion(Double gastoPublicacion) {
        this.gastoPublicacion = gastoPublicacion;
    }

    public Double getGastosCierre() {
        return gastosCierre;
    }

    public void setGastosCierre(Double gastosCierre) {
        this.gastosCierre = gastosCierre;
    }

    public Double getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(Double porcentajeComision) {
        this.porcentajeComision = porcentajeComision;
    }

    public Double getPorcentajeReinversion() {
        return porcentajeReinversion;
    }

    public void setPorcentajeReinversion(Double porcentajeReinversion) {
        this.porcentajeReinversion = porcentajeReinversion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public double getCostoTotalCalculado() {
        return valor(costoBase)
                + valor(costoAcondicionamiento)
                + valor(costoTraslado)
                + valor(costoAdministrativo)
                + valor(gastoPublicacion)
                + valor(gastosCierre);
    }

    public double getUtilidadBrutaCalculada() {
        return valor(precioVentaFinal) - getCostoTotalCalculado();
    }

    public double getValorComisionCalculado() {
        return Math.max(getUtilidadBrutaCalculada(), 0.0) * valor(porcentajeComision) / 100.0;
    }

    public double getUtilidadNetaCalculada() {
        return getUtilidadBrutaCalculada() - getValorComisionCalculado();
    }

    public double getValorReinversionCalculado() {
        return Math.max(getUtilidadNetaCalculada(), 0.0) * valor(porcentajeReinversion) / 100.0;
    }

    public double getMargenCalculado() {
        double venta = valor(precioVentaFinal);
        if (venta <= 0.0) {
            return 0.0;
        }
        return (getUtilidadNetaCalculada() * 100.0) / venta;
    }

    private double valor(Double numero) {
        return numero == null ? 0.0 : numero;
    }
}