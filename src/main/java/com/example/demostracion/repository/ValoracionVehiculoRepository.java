package com.example.demostracion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.ValoracionVehiculo;

public interface ValoracionVehiculoRepository extends JpaRepository<ValoracionVehiculo, Long> {

    Optional<ValoracionVehiculo> findByVehiculoIdVehiculo(Long vehiculoId);
}