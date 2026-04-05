package com.example.demostracion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demostracion.model.Cliente;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCorreo(String correo);
    Optional<Cliente> findByCedula(String cedula);
    List<Cliente> findByActivoTrue();
    List<Cliente> findByEstado(String estado);
    List<Cliente> findByActivoTrueAndEstado(String estado);
}
