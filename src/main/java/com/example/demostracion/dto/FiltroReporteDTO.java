package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroReporteDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String marca;
    private String modelo;
    private String color;
    private String estadoLogistico;
    private String ubicacion;
    private Integer anioDesde;
    private Integer anioHasta;
    private Boolean soloActivos;
}
