package com.example.demostracion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "correo", nullable = false, unique = true)
    private String correo;
    
    @Column(name = "telefono")
    private String telefono;
    
    @Column(name = "cedula", unique = true)
    private String cedula;
    
    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "barrio")
    private String barrio;

    @Column(name = "localidad")
    private String localidad;
    
    @Column(name = "direccion")
    private String direccion;
    
    @Column(name = "interes_vehiculo")
    private String interesVehiculo;
    
    @Column(name = "presupuesto")
    private Double presupuesto;
    
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_ultima_interaccion")
    private LocalDateTime fechaUltimaInteraccion;
    
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;
    
    @Column(name = "activo")
    private boolean activo = true;
    
    public Cliente() {
        this.estado = "Nuevo";
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
    
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getInteresVehiculo() { return interesVehiculo; }
    public void setInteresVehiculo(String interesVehiculo) { this.interesVehiculo = interesVehiculo; }
    
    public Double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(Double presupuesto) { this.presupuesto = presupuesto; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public LocalDateTime getFechaUltimaInteraccion() { return fechaUltimaInteraccion; }
    public void setFechaUltimaInteraccion(LocalDateTime fechaUltimaInteraccion) { this.fechaUltimaInteraccion = fechaUltimaInteraccion; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
