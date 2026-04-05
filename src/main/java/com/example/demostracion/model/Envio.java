package com.example.demostracion.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origen;
    private String destino;
    private String estado;
    private String eta;
}

