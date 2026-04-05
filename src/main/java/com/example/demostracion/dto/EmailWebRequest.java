package com.example.demostracion.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmailWebRequest {
    
    private List<String> destinatarios;
    private String asunto;
    private String mensaje;
    private boolean esHtml = false;
    private List<String> archivosAdjuntos;
    
    // Método para convertir a EmailRequest
    public EmailRequest toEmailRequest() {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setDestinatarios(this.destinatarios);
        emailRequest.setAsunto(this.asunto);
        emailRequest.setMensaje(this.mensaje);
        emailRequest.setEsHtml(this.esHtml);
        emailRequest.setArchivosAdjuntos(this.archivosAdjuntos);
        return emailRequest;
    }
}