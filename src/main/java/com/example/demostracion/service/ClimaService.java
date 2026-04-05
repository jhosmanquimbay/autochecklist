package com.example.demostracion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ClimaService {

    private final String API_KEY = "0e9c710ebdb6c5dc45756e46713e116d";  
    private final String URL = "https://api.openweathermap.org/data/2.5/weather?q={ciudad}&appid={apiKey}&units=metric&lang=es";

    public Map<String, Object> obtenerClima(String ciudad) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            log.info("Consultando clima para la ciudad: {}", ciudad);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(URL, Map.class, ciudad, API_KEY);
            
            if (response != null) {
                log.info("Clima obtenido exitosamente para: {}", ciudad);
                return response;
            } else {
                log.warn("Respuesta vacía de la API para la ciudad: {}", ciudad);
                return crearRespuestaError("No se pudo obtener información del clima");
            }
            
        } catch (RestClientException e) {
            log.error("Error al consultar la API de clima para la ciudad: {}", ciudad, e);
            
            // Verificar si es un error 404 (ciudad no encontrada)
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                return crearRespuestaError("Ciudad no encontrada. Verifica el nombre e intenta nuevamente.");
            }
            
            return crearRespuestaError("Error de conexión con el servicio de clima");
        } catch (Exception e) {
            log.error("Error inesperado al obtener clima para: {}", ciudad, e);
            return crearRespuestaError("Error interno del servidor");
        }
    }
    
    private Map<String, Object> crearRespuestaError(String mensaje) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("cod", "404");
        errorResponse.put("message", mensaje);
        return errorResponse;
    }
}
