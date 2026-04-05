package com.example.demostracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EmailRequest {
    
    @NotEmpty(message = "La lista de destinatarios no puede estar vacía")
    @Size(min = 1, max = 100, message = "Debe haber entre 1 y 100 destinatarios")
    private List<String> destinatarios;
    
    @NotBlank(message = "El asunto no puede estar vacío")
    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String asunto;
    
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 5000, message = "El mensaje no puede exceder 5000 caracteres")
    private String mensaje;
    
    private boolean esHtml = false;
    
    // Lista de nombres de archivos adjuntos (opcional)
    private List<String> archivosAdjuntos;
}