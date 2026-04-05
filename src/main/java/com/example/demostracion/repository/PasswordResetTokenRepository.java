package com.example.demostracion.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demostracion.model.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findTopByUsuarioIdUsuarioAndFechaUsoIsNullOrderByFechaSolicitudDesc(Long usuarioId);

    void deleteByUsuarioIdUsuario(Long usuarioId);

    void deleteByFechaExpiracionBefore(LocalDateTime fecha);
}