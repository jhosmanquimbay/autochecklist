package com.example.demostracion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Notificacion;
import com.example.demostracion.repository.NotificacionRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    // Listar todas (para gerente)
    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAll();
    }

    // Listar por conductor (para panel conductor)
    public List<Notificacion> listarPorConductor(Conductor conductor) {
        return notificacionRepository.findByConductor(conductor);
    }

    // Guardar nueva notificación
    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    // Buscar por ID
    public Optional<Notificacion> buscarPorId(Long id) {
        return notificacionRepository.findById(id);
    }

    // Eliminar
    public void eliminar(Long id) {
        notificacionRepository.deleteById(id);
    }

    // ✅ Marcar como leída
    public Notificacion marcarComoLeida(Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + id));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    // ✅ Listar por rango de fechas
    public List<Notificacion> listarPorFechas(LocalDateTime desde, LocalDateTime hasta) {
        return notificacionRepository.findAll().stream()
                .filter(n -> n.getFecha() != null
                        && !n.getFecha().isBefore(desde)
                        && !n.getFecha().isAfter(hasta))
                .toList();
    }
}
