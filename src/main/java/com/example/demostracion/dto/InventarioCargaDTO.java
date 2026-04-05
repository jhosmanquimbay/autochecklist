package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioCargaDTO {
    private int fila;
    private String chasis;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private String motor;
    private String ubicacionActual;
    private String estadoLogistico;
    private String error;
    private boolean valido;
}
