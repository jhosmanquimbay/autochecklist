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
@Table(name = "solicitudes_prueba")
public class SolicitudPrueba {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    
    @ManyToOne
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;
    
    @Column(name = "fecha_solicitada", nullable = false)
    private LocalDateTime fechaSolicitada;
    
    @Column(name = "fecha_aprobada")
    private LocalDateTime fechaAprobada;
    
    @Column(name = "fecha_realizacion")
    private LocalDateTime fechaRealizacion;
    
    @Column(name = "estado", nullable = false)
    private String estado;
    
    @Column(name = "resultado_prueba")
    private String resultadoPrueba;
    
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
    
    @Column(name = "fecha_contacto_seguimiento")
    private LocalDateTime fechaContactoSeguimiento;
    
    public SolicitudPrueba() {
        this.estado = "Pendiente";
        this.fechaSolicitada = LocalDateTime.now();
    }
    
    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }
    
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    
    public LocalDateTime getFechaSolicitada() { return fechaSolicitada; }
    public void setFechaSolicitada(LocalDateTime fechaSolicitada) { this.fechaSolicitada = fechaSolicitada; }
    
    public LocalDateTime getFechaAprobada() { return fechaAprobada; }
    public void setFechaAprobada(LocalDateTime fechaAprobada) { this.fechaAprobada = fechaAprobada; }
    
    public LocalDateTime getFechaRealizacion() { return fechaRealizacion; }
    public void setFechaRealizacion(LocalDateTime fechaRealizacion) { this.fechaRealizacion = fechaRealizacion; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getResultadoPrueba() { return resultadoPrueba; }
    public void setResultadoPrueba(String resultadoPrueba) { this.resultadoPrueba = resultadoPrueba; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    public LocalDateTime getFechaContactoSeguimiento() { return fechaContactoSeguimiento; }
    public void setFechaContactoSeguimiento(LocalDateTime fechaContactoSeguimiento) { this.fechaContactoSeguimiento = fechaContactoSeguimiento; }
}
