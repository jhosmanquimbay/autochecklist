package com.example.demostracion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoCargaInventarioDTO {
    private int totalFilas;
    private int filasExitosas;
    private int filasConError;
    private List<InventarioCargaDTO> errores = new ArrayList<>();
    private boolean exitoso;
    private String mensaje;
}
