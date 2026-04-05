package com.example.demostracion.controller;

import com.example.demostracion.dto.EmailRequest;
import com.example.demostracion.dto.EmailResponse;
import com.example.demostracion.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {
    
    private final EmailService emailService;
    
    /**
     * Envía correos masivos de forma síncrona
     */
    @PostMapping("/enviar")
    public ResponseEntity<EmailResponse> enviarCorreosMasivos(@Valid @RequestBody EmailRequest request) {
        try {
            log.info("Recibida solicitud de envío masivo para {} destinatarios", 
                    request.getDestinatarios().size());
            
            EmailResponse response = emailService.enviarCorreosMasivos(request);
            
            if (response.isExitoso()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error en envío masivo de correos", e);
            EmailResponse errorResponse = EmailResponse.error("Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Envía correos masivos de forma asíncrona
     */
    @PostMapping("/enviar-async")
    public ResponseEntity<String> enviarCorreosMasivosAsync(@Valid @RequestBody EmailRequest request) {
        try {
            log.info("Recibida solicitud de envío masivo asíncrono para {} destinatarios", 
                    request.getDestinatarios().size());
            
            CompletableFuture<EmailResponse> futureResponse = emailService.enviarCorreosMasivosAsync(request);
            
            // Procesar respuesta asíncrona (opcional: guardar en base de datos, notificar, etc.)
            futureResponse.thenAccept(response -> {
                log.info("Envío asíncrono completado: {} enviados, {} fallidos", 
                        response.getTotalEnviados(), response.getTotalFallidos());
            });
            
            return ResponseEntity.accepted()
                    .body("Solicitud de envío masivo recibida. Los correos se están procesando de forma asíncrona.");
            
        } catch (Exception e) {
            log.error("Error en envío masivo asíncrono", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * Envía correos masivos con archivos adjuntos
     */
    @PostMapping("/enviar-con-adjuntos")
    public ResponseEntity<EmailResponse> enviarCorreosConAdjuntos(
            @Valid @RequestPart("email") EmailRequest request,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos) {
        
        try {
            log.info("Recibida solicitud de envío con adjuntos para {} destinatarios", 
                    request.getDestinatarios().size());
            
            if (archivos != null) {
                log.info("Archivos adjuntos recibidos: {}", archivos.size());
            }
            
            EmailResponse response = emailService.enviarCorreosConAdjuntos(request, archivos);
            
            if (response.isExitoso()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error en envío con adjuntos", e);
            EmailResponse errorResponse = EmailResponse.error("Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Valida la configuración de correo
     */
    @GetMapping("/validar-configuracion")
    public ResponseEntity<String> validarConfiguracion() {
        try {
            boolean esValida = emailService.validarConfiguracion();
            
            if (esValida) {
                return ResponseEntity.ok("Configuración de correo válida");
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Configuración de correo inválida");
            }
            
        } catch (Exception e) {
            log.error("Error validando configuración", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validando configuración: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint de prueba para verificar que el servicio está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de correo funcionando correctamente");
    }
}