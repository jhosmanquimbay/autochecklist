package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoCargaDTO {
    private int fila;
    private String chasis;
    private String modelo;
    private Long inventarioId;
    private String error;
    private boolean valido;
}
