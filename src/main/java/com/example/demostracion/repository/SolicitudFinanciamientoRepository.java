package com.example.demostracion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.SolicitudFinanciamiento;

public interface SolicitudFinanciamientoRepository extends JpaRepository<SolicitudFinanciamiento, Long> {

    List<SolicitudFinanciamiento> findTop20ByOrderByFechaUltimaGestionDescFechaSimulacionDesc();

    List<SolicitudFinanciamiento> findTop20ByAsesorCorreoIgnoreCaseOrderByFechaUltimaGestionDescFechaSimulacionDesc(String asesorCorreo);

    List<SolicitudFinanciamiento> findByAsesorCorreoIgnoreCase(String asesorCorreo);

    long countByEstadoAnalisisIgnoreCase(String estadoAnalisis);

    long countByCanalOrigenIgnoreCase(String canalOrigen);
}