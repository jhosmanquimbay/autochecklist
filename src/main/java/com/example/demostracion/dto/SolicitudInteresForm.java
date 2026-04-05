package com.example.demostracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class SolicitudInteresForm {

    private Long vehiculoId;

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombreCompleto;

    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
    private String telefono;

    @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres.")
    private String mensaje;

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}