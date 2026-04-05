package com.example.demostracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ValidarCodigoRecuperacionForm {

    @NotBlank(message = "Ingresa el correo asociado a tu cuenta.")
    @Email(message = "Ingresa un correo válido.")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres.")
    private String correo;

    @NotBlank(message = "Ingresa el código enviado al correo.")
    @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos.")
    private String codigo;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}