package com.example.demostracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RecuperarContrasenaForm {

    @NotBlank(message = "Ingresa el correo registrado.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    private String correo;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}