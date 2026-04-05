package com.example.demostracion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.Conductor;
import com.example.demostracion.model.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByConductor(Conductor conductor);
}
