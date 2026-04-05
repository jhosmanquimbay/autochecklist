package com.example.demostracion.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demostracion.dto.FinanciamientoResultadoDTO;
import com.example.demostracion.dto.FinanciamientoSolicitudForm;
import com.example.demostracion.service.FinanciamientoService;

@RestController
@RequestMapping("/api/financiamiento")
public class FinanciamientoApiController {

    private final FinanciamientoService financiamientoService;

    public FinanciamientoApiController(FinanciamientoService financiamientoService) {
        this.financiamientoService = financiamientoService;
    }

    @GetMapping("/vehiculos/{id}/referencia")
    public ResponseEntity<?> obtenerReferencia(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(financiamientoService.calcularOfertaReferencial(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/vehiculos/{id}/simular")
    public ResponseEntity<?> simular(@PathVariable Long id,
                                     @RequestBody FinanciamientoSolicitudForm form,
                                     Authentication authentication) {
        try {
            form.setVehiculoId(id);
            String correoAutenticado = authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getName() != null
                    && !"anonymousUser".equalsIgnoreCase(authentication.getName())
                    ? authentication.getName()
                    : null;
            FinanciamientoResultadoDTO resultado = financiamientoService.registrarSimulacion(
                    id,
                    form,
                    form.getCanalOrigen(),
                    null,
                    null,
                    correoAutenticado);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}