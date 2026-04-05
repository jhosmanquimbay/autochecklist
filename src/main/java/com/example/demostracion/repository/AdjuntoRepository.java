package com.example.demostracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demostracion.model.Adjunto;

import java.util.List;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {
    List<Adjunto> findByMensajeId(Long mensajeId);
}
