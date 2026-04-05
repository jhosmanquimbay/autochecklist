package com.example.demostracion.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    // Encontrar todos los vehículos activos
    List<Vehiculo> findByActivoTrue();
    
    // Encontrar todos los vehículos inactivos
    List<Vehiculo> findByActivoFalse();
    
    // Verificar si existe un vehículo con el chasis dado
    boolean existsByChasis(String chasis);

    boolean existsByChasisIgnoreCase(String chasis);

    boolean existsByChasisIgnoreCaseAndIdVehiculoNot(String chasis, Long idVehiculo);

    Optional<Vehiculo> findByChasis(String chasis);

    List<Vehiculo> findAllByOrderByFechaCreacionDesc();

    List<Vehiculo> findByEstadoPublicacionIgnoreCaseOrderByFechaCreacionDesc(String estadoPublicacion);

    long countByEstadoPublicacionIgnoreCase(String estadoPublicacion);
}
