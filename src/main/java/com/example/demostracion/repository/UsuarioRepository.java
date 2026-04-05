package com.example.demostracion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demostracion.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por correo (para login)
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCedula(String cedula);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCedula(String cedula);

}
