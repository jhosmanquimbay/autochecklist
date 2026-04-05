package com.example.demostracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClientePerfilForm {

    @NotBlank(message = "La cédula es obligatoria.")
    @Pattern(regexp = "^[0-9]{5,20}$", message = "La cédula solo puede contener números.")
    private String cedula;

    @NotBlank(message = "El número de teléfono es obligatorio.")
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Ingresa un teléfono válido.")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido.")
    private String correo;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombreCompleto;

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres.")
    private String ciudad;

    @Size(max = 100, message = "El barrio no puede superar los 100 caracteres.")
    private String barrio;

    @Size(max = 100, message = "La localidad no puede superar los 100 caracteres.")
    private String localidad;

    @Pattern(
            regexp = "(^$)|^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$",
            message = "La contraseña debe tener entre 8 y 16 caracteres, una mayúscula, un número y un carácter especial."
    )
    private String nuevaContrasena;

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }
}