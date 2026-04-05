package com.example.demostracion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demostracion.model.FavoritoVehiculo;

public interface FavoritoVehiculoRepository extends JpaRepository<FavoritoVehiculo, Long> {

    List<FavoritoVehiculo> findByUsuarioIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);

    Optional<FavoritoVehiculo> findByUsuarioIdUsuarioAndVehiculoIdVehiculo(Long idUsuario, Long idVehiculo);

    boolean existsByUsuarioIdUsuarioAndVehiculoIdVehiculo(Long idUsuario, Long idVehiculo);

    void deleteByUsuarioIdUsuarioAndVehiculoIdVehiculo(Long idUsuario, Long idVehiculo);
}