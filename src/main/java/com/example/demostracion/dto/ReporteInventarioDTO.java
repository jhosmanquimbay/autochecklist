package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteInventarioDTO {
    private int totalVehiculos;
    private int vehiculosActivos;
    private int vehiculosInactivos;
    private Map<String, Long> vehiculosPorMarca;
    private Map<String, Long> vehiculosPorEstadoLogistico;
    private Map<String, Long> vehiculosPorColor;
    private Map<Integer, Long> vehiculosPorAnio;
    private Map<String, Long> vehiculosPorUbicacion;
    private LocalDateTime fechaGeneracion;
    private String generadoPor;
    private String filtrosAplicados;
}
