package com.example.demostracion.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.service.EmailService;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.example.demostracion.service.InboundMailService inboundMailService;

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> testEmailConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = emailService.validarConfiguracion();
            response.put("success", isValid);
            response.put("message", isValid ? 
                "✅ Configuración de correo válida" : 
                "❌ Error en configuración de correo");
            
            if (isValid) {
                response.put("smtp_status", "Conectado");
                response.put("recommendation", "Configuración correcta. Los correos deberían funcionar.");
            } else {
                response.put("smtp_status", "Error de conexión");
                response.put("recommendation", "Verifica las credenciales de Gmail y genera nueva contraseña de aplicación");
                response.put("help_url", "https://myaccount.google.com/security");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Error: " + e.getMessage());
            response.put("smtp_status", "Error");
            response.put("error_details", e.getClass().getSimpleName());
            
            if (e.getMessage().contains("Authentication")) {
                response.put("recommendation", "🔧 Genera nueva contraseña de aplicación en Gmail");
                response.put("help_url", "https://myaccount.google.com/security");
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/force-inbox-check")
    public ResponseEntity<Map<String, Object>> forceInboxCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("[TestEmailController] 🔄 Forzando verificación manual de correos entrantes...");
            inboundMailService.pollInbox();
            
            response.put("success", true);
            response.put("message", "✅ Verificación de correos entrantes ejecutada");
            response.put("recommendation", "Revisa los logs de la consola para ver el resultado");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Error al verificar correos: " + e.getMessage());
            response.put("error_details", e.getClass().getSimpleName());
            
            if (e.getMessage().contains("Authentication")) {
                response.put("recommendation", "🔧 Problema de autenticación con Gmail");
                response.put("help_url", "https://myaccount.google.com/security");
            }
        }
        
        return ResponseEntity.ok(response);
    }
}