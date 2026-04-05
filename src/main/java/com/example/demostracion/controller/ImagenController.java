package com.example.demostracion.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import com.example.demostracion.model.Vehiculo;
import com.example.demostracion.repository.VehiculoRepository;

@RestController
public class ImagenController {

    private final VehiculoRepository vehiculoRepository;

    public ImagenController(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    @GetMapping("/imagenes/{id}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id) {

        return obtenerImagenPorPosicion(id, 1);
    }

    @GetMapping("/imagenes/{id}/{posicion}")
    public ResponseEntity<byte[]> obtenerImagenPorPosicion(@PathVariable Long id, @PathVariable Integer posicion) {

        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);
        byte[] data = obtenerImagenSegunPosicion(vehiculo, posicion);

        if (vehiculo == null || data == null) {
            return ResponseEntity.notFound().build();
        }

        String tipo = "application/octet-stream";
        try (java.io.InputStream is = new java.io.ByteArrayInputStream(data)) {
            String guessed = java.net.URLConnection.guessContentTypeFromStream(is);
            if (guessed != null) {
                tipo = guessed;
            }
        } catch (IOException e) {
            // ignore, usaremos el default
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(tipo))
                .body(data);
    }

    private byte[] obtenerImagenSegunPosicion(Vehiculo vehiculo, Integer posicion) {
        if (vehiculo == null) {
            return null;
        }

        int slot = posicion == null ? 1 : posicion;
        return switch (slot) {
            case 2 -> vehiculo.getImagen2();
            case 3 -> vehiculo.getImagen3();
            case 4 -> vehiculo.getImagen4();
            default -> vehiculo.getImagen();
        };
    }
}