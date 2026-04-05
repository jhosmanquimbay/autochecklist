package com.example.demostracion.service;

import com.example.demostracion.dto.FiltroReporteDTO;
import com.example.demostracion.dto.ReporteInventarioDTO;
import com.example.demostracion.model.Inventario;
import com.example.demostracion.repository.InventarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteInventarioService {

    private final InventarioRepository inventarioRepository;

    public ReporteInventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    public ReporteInventarioDTO generarReporte(FiltroReporteDTO filtro) {
        List<Inventario> inventarios = obtenerInventariosFiltrados(filtro);

        ReporteInventarioDTO reporte = new ReporteInventarioDTO();
        
        reporte.setTotalVehiculos(inventarios.size());
        reporte.setVehiculosActivos((int) inventarios.stream().filter(Inventario::isActivo).count());
        reporte.setVehiculosInactivos((int) inventarios.stream().filter(inv -> !inv.isActivo()).count());

        reporte.setVehiculosPorMarca(
            inventarios.stream()
                .filter(inv -> inv.getMarca() != null && !inv.getMarca().isEmpty())
                .collect(Collectors.groupingBy(Inventario::getMarca, Collectors.counting()))
        );

        reporte.setVehiculosPorEstadoLogistico(
            inventarios.stream()
                .filter(inv -> inv.getEstadoLogistico() != null && !inv.getEstadoLogistico().isEmpty())
                .collect(Collectors.groupingBy(Inventario::getEstadoLogistico, Collectors.counting()))
        );

        reporte.setVehiculosPorColor(
            inventarios.stream()
                .filter(inv -> inv.getColor() != null && !inv.getColor().isEmpty())
                .collect(Collectors.groupingBy(Inventario::getColor, Collectors.counting()))
        );

        reporte.setVehiculosPorAnio(
            inventarios.stream()
                .filter(inv -> inv.getAnio() != null)
                .collect(Collectors.groupingBy(Inventario::getAnio, Collectors.counting()))
        );

        reporte.setVehiculosPorUbicacion(
            inventarios.stream()
                .filter(inv -> inv.getUbicacionActual() != null && !inv.getUbicacionActual().isEmpty())
                .collect(Collectors.groupingBy(Inventario::getUbicacionActual, Collectors.counting()))
        );

        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setFiltrosAplicados(construirDescripcionFiltros(filtro));

        return reporte;
    }

    private List<Inventario> obtenerInventariosFiltrados(FiltroReporteDTO filtro) {
        List<Inventario> inventarios;

        if (filtro.getSoloActivos() != null && filtro.getSoloActivos()) {
            inventarios = inventarioRepository.findByActivoTrue();
        } else {
            inventarios = inventarioRepository.findAll();
        }

        return inventarios.stream()
            .filter(inv -> filtro.getMarca() == null || filtro.getMarca().isEmpty() || 
                          inv.getMarca().toLowerCase().contains(filtro.getMarca().toLowerCase()))
            .filter(inv -> filtro.getModelo() == null || filtro.getModelo().isEmpty() || 
                          inv.getModelo().toLowerCase().contains(filtro.getModelo().toLowerCase()))
            .filter(inv -> filtro.getColor() == null || filtro.getColor().isEmpty() || 
                          (inv.getColor() != null && inv.getColor().toLowerCase().contains(filtro.getColor().toLowerCase())))
            .filter(inv -> filtro.getEstadoLogistico() == null || filtro.getEstadoLogistico().isEmpty() || 
                          (inv.getEstadoLogistico() != null && inv.getEstadoLogistico().equalsIgnoreCase(filtro.getEstadoLogistico())))
            .filter(inv -> filtro.getUbicacion() == null || filtro.getUbicacion().isEmpty() || 
                          (inv.getUbicacionActual() != null && inv.getUbicacionActual().toLowerCase().contains(filtro.getUbicacion().toLowerCase())))
            .filter(inv -> filtro.getAnioDesde() == null || 
                          (inv.getAnio() != null && inv.getAnio() >= filtro.getAnioDesde()))
            .filter(inv -> filtro.getAnioHasta() == null || 
                          (inv.getAnio() != null && inv.getAnio() <= filtro.getAnioHasta()))
            .collect(Collectors.toList());
    }

    private String construirDescripcionFiltros(FiltroReporteDTO filtro) {
        StringBuilder sb = new StringBuilder();
        
        if (filtro.getMarca() != null && !filtro.getMarca().isEmpty()) {
            sb.append("Marca: ").append(filtro.getMarca()).append("; ");
        }
        if (filtro.getModelo() != null && !filtro.getModelo().isEmpty()) {
            sb.append("Modelo: ").append(filtro.getModelo()).append("; ");
        }
        if (filtro.getColor() != null && !filtro.getColor().isEmpty()) {
            sb.append("Color: ").append(filtro.getColor()).append("; ");
        }
        if (filtro.getEstadoLogistico() != null && !filtro.getEstadoLogistico().isEmpty()) {
            sb.append("Estado: ").append(filtro.getEstadoLogistico()).append("; ");
        }
        if (filtro.getUbicacion() != null && !filtro.getUbicacion().isEmpty()) {
            sb.append("Ubicación: ").append(filtro.getUbicacion()).append("; ");
        }
        if (filtro.getAnioDesde() != null) {
            sb.append("Año desde: ").append(filtro.getAnioDesde()).append("; ");
        }
        if (filtro.getAnioHasta() != null) {
            sb.append("Año hasta: ").append(filtro.getAnioHasta()).append("; ");
        }
        if (filtro.getSoloActivos() != null && filtro.getSoloActivos()) {
            sb.append("Solo activos; ");
        }
        
        return sb.length() > 0 ? sb.toString() : "Sin filtros aplicados";
    }
}
