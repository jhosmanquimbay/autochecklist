package com.example.demostracion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demostracion.model.Mensaje;
import com.example.demostracion.model.Usuario;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // =============================
    // BANDEJA ENTRADA
    // =============================
    List<Mensaje> findByDestinatarioAndEliminadoFalseOrderByFechaEnvioDesc(
            Usuario destinatario);

    // =============================
    // ENVIADOS
    // =============================
    List<Mensaje> findByRemitenteAndEliminadoFalseOrderByFechaEnvioDesc(
            Usuario remitente);

    // =============================
    // POR CARPETA
    // =============================
    List<Mensaje> findByDestinatarioAndCarpetaAndEliminadoFalseOrderByFechaEnvioDesc(
            Usuario destinatario,
            String carpeta);

    List<Mensaje> findByRemitenteAndCarpetaAndEliminadoFalseOrderByFechaEnvioDesc(
            Usuario remitente,
            String carpeta);

    // Búsquedas sin filtrar por eliminado (para debug / herramientas internas)
    List<Mensaje> findByDestinatarioAndCarpetaOrderByFechaEnvioDesc(
            Usuario destinatario,
            String carpeta);

    List<Mensaje> findByRemitenteAndCarpetaOrderByFechaEnvioDesc(
            Usuario remitente,
            String carpeta);

    // =============================
    // CONVERSACIONES (CHAT)
    // =============================
    @Query("""
        SELECT m FROM Mensaje m
        WHERE
        (m.remitente = :u1 AND m.destinatario = :u2)
        OR
        (m.remitente = :u2 AND m.destinatario = :u1)
        ORDER BY m.fechaEnvio ASC
    """)
    List<Mensaje> findConversacion(Usuario u1, Usuario u2);

    // =============================
    // RESPUESTAS
    // =============================
    List<Mensaje> findByIdPadreOrderByFechaEnvioAsc(Long idPadre);

    // =============================
    // CORREOS EXTERNOS
    // =============================
    List<Mensaje> findByDestinatarioExternoOrderByFechaEnvioDesc(
            String destinatarioExterno);

    List<Mensaje> findByRemitente_CorreoOrderByFechaEnvioDesc(
            String correo);

    // =============================
    // NO LEÍDOS
    // =============================
    long countByDestinatarioAndLeidoFalseAndEliminadoFalse(
            Usuario usuario);

    List<Mensaje> findByDestinatarioAndLeidoFalseAndEliminadoFalse(
            Usuario usuario);

    // =============================
    // PAPELERA
    // =============================
    List<Mensaje> findByDestinatarioAndEliminadoTrueOrderByFechaEnvioDesc(
            Usuario destinatario);

                @Query("""
                                SELECT DISTINCT m FROM Mensaje m
                                WHERE m.eliminado = true
                                        AND m.eliminadoPermanente = false
                                        AND (
                                                                (m.destinatario IS NOT NULL AND m.destinatario.idUsuario = :usuarioId)
                                                 OR (m.remitente IS NOT NULL AND m.remitente.idUsuario = :usuarioId)
                                        )
                                ORDER BY m.fechaEnvio DESC
                """)
                List<Mensaje> findTrashByUsuarioId(@Param("usuarioId") Long usuarioId);

                @Query("""
                                SELECT DISTINCT m FROM Mensaje m
                                WHERE m.eliminado = true
                                        AND (
                                                                (m.destinatario IS NOT NULL AND m.destinatario.idUsuario = :usuarioId)
                                                 OR (m.remitente IS NOT NULL AND m.remitente.idUsuario = :usuarioId)
                                        )
                                ORDER BY m.fechaEnvio DESC
                """)
                List<Mensaje> findDeletedByUsuarioId(@Param("usuarioId") Long usuarioId);

    // =============================
    // DEBUG
    // =============================
    List<Mensaje> findTop10ByOrderByFechaEnvioDesc();

    long countByCarpeta(String carpeta);

    // Métodos para debug / herramientas internas
    List<Mensaje> findByRemitenteOrderByFechaEnvioDesc(Usuario remitente);
    List<Mensaje> findByDestinatarioOrderByFechaEnvioDesc(Usuario destinatario);
    List<Mensaje> findByCarpetaOrderByFechaEnvioDesc(String carpeta);
        List<Mensaje> findByEliminadoTrueAndEliminadoPermanenteFalseOrderByFechaEnvioDesc();
}