package com.example.demostracion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demostracion.model.Conductor;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {

    // Buscar conductor por número de licencia (único)
    Optional<Conductor> findByLicencia(String licencia);

    // Buscar conductores cuyo nombre contenga la cadena (ignore case)
    List<Conductor> findByNombreContainingIgnoreCase(String nombre);

    // Buscar conductores asignados a un vehículo (usa propiedad anidada idVehiculo)
    List<Conductor> findByVehiculo_IdVehiculo(Long idVehiculo);

    // Buscar conductores sin vehículo asignado
    List<Conductor> findByVehiculoIsNull();

    // Buscar conductor por username (ligado al usuario del login)
    Optional<Conductor> findByUsername(String username);
}
