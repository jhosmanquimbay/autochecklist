package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    
    private boolean exitoso;
    private String mensaje;
    private int totalEnviados;
    private int totalFallidos;
    private List<String> errores;
    private LocalDateTime fechaEnvio;
    
    public static EmailResponse exitoso(int totalEnviados, LocalDateTime fechaEnvio) {
        return new EmailResponse(true, "Correos enviados exitosamente", 
                               totalEnviados, 0, null, fechaEnvio);
    }
    
    public static EmailResponse conErrores(int totalEnviados, int totalFallidos, 
                                         List<String> errores, LocalDateTime fechaEnvio) {
        return new EmailResponse(false, "Algunos correos fallaron", 
                               totalEnviados, totalFallidos, errores, fechaEnvio);
    }
    
    public static EmailResponse error(String mensaje) {
        return new EmailResponse(false, mensaje, 0, 0, null, LocalDateTime.now());
    }
}