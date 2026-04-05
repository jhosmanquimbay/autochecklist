package com.example.demostracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerContrasenaForm {

    @NotBlank(message = "Ingresa una nueva contraseña.")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres.")
    private String nuevaContrasena;

    @NotBlank(message = "Confirma la nueva contraseña.")
    private String confirmacionContrasena;

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmacionContrasena() {
        return confirmacionContrasena;
    }

    public void setConfirmacionContrasena(String confirmacionContrasena) {
        this.confirmacionContrasena = confirmacionContrasena;
    }
}