package com.example.demostracion.dto;

import java.util.List;

public record RecallResumenDTO(
        boolean exito,
        String mensaje,
        int totalCampanias,
        String nivelRiesgo,
        List<RecallItemDTO> items) {
}