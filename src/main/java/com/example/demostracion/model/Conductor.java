package com.example.demostracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "conductor")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conductor")
    private Long idConductor;

    // 👇 Aquí guardamos el "correo" del usuario como username
    @Column(name = "username", nullable = false, unique = true, length = 150)
    private String username; 

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "licencia", length = 50)
    private String licencia;

    @Column(name = "telefono", length = 20)
    private String telefono;

    // Relación con vehículo
    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = true)
    private Vehiculo vehiculo;

    // ===== Getters y Setters =====
    public Long getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(Long idConductor) {
        this.idConductor = idConductor;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }
}
