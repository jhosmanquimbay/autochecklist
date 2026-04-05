package com.example.demostracion.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demostracion.model.Adjunto;
import com.example.demostracion.repository.AdjuntoRepository;

@Service
public class AdjuntoService {

    private final AdjuntoRepository adjuntoRepository;

    public AdjuntoService(AdjuntoRepository adjuntoRepository) {
        this.adjuntoRepository = adjuntoRepository;
    }

    public ResponseEntity<byte[]> descargarAdjunto(Long id) {
        Optional<Adjunto> maybe = adjuntoRepository.findById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Adjunto a = maybe.get();
        File f = new File(a.getRutaArchivo());
        if (!f.exists()) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] data = Files.readAllBytes(f.toPath());
            String filename = a.getNombreArchivo() != null ? a.getNombreArchivo() : f.getName();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
