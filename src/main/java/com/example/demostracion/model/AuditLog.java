package com.example.demostracion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para auditoría de acciones de usuario
 * 
 * ISO 25010: Seguridad - No Repudio
 * ✅ Registro de quién hizo qué y cuándo
 * ✅ Trazabilidad completa de operaciones
 * 
 * @author Sistema
 * @since 1.0
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String accion;

    @Column(nullable = false)
    private String entidad;

    @Column(name = "id_entidad", nullable = false)
    private Long idEntidad;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private String resultado; // EXITOSO, ERROR

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    // ========== CONSTRUCTORES ==========
    public AuditLog() {}

    public AuditLog(String usuario, String accion, String entidad, Long idEntidad) {
        this.usuario = usuario;
        this.accion = accion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.timestamp = LocalDateTime.now();
        this.resultado = "EXITOSO";
    }

    // ========== GETTERS Y SETTERS ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public Long getIdEntidad() { return idEntidad; }
    public void setIdEntidad(Long idEntidad) { this.idEntidad = idEntidad; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
}
