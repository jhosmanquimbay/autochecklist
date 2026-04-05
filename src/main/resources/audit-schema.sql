-- ===========================================
-- TABLA DE AUDITORÍA
-- ISO 25010: Seguridad - No Repudio
-- ===========================================
-- Registra todas las operaciones críticas
-- para trazabilidad y auditoría

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(100) NOT NULL,
    id_entidad BIGINT NOT NULL,
    detalles LONGTEXT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    resultado VARCHAR(20) NOT NULL DEFAULT 'EXITOSO',
    stack_trace LONGTEXT,
    
    -- Índices para búsquedas rápidas
    INDEX idx_usuario (usuario),
    INDEX idx_timestamp (timestamp),
    INDEX idx_entidad (entidad, id_entidad),
    INDEX idx_accion (accion),
    INDEX idx_resultado (resultado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- TABLA DE EVENTOS RESILIENCE4J
-- ISO 25010: Confiabilidad - Monitoreo
-- ===========================================
-- Registra fallos y recuperaciones de sistemas

CREATE TABLE IF NOT EXISTS event_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL, -- RETRY, CIRCUIT_BREAKER, TIMEOUT
    service_name VARCHAR(100) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILURE, RECOVERING
    error_message LONGTEXT,
    attempt_number INT,
    
    INDEX idx_service (service_name),
    INDEX idx_timestamp (timestamp),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- TABLA DE SESIONES DE USUARIO
-- ISO 25010: Seguridad - Autenticación
-- ===========================================
-- Para rastrear sesiones activas y fallidas

CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_acceso DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    es_activa BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_activa (es_activa),
    INDEX idx_expiracion (fecha_expiracion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- TABLA DE INTENTOS FALLIDOS DE LOGIN
-- ISO 25010: Seguridad - Protección contra fuerza bruta
-- ===========================================

CREATE TABLE IF NOT EXISTS failed_login_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correo VARCHAR(150) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    razon VARCHAR(255),
    
    INDEX idx_correo (correo),
    INDEX idx_ip (ip_address),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- TABLA DE CAMBIOS DE SENSIBLES
-- ISO 25010: Seguridad - Integridad
-- ===========================================
-- Registra cambios en datos sensibles

CREATE TABLE IF NOT EXISTS sensitive_data_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tabla VARCHAR(100) NOT NULL,
    columna VARCHAR(100) NOT NULL,
    valor_anterior VARCHAR(500),
    valor_nuevo VARCHAR(500),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    razon VARCHAR(500),
    
    FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_tabla (tabla),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===========================================
-- Crear vista para reportes
-- ===========================================

CREATE OR REPLACE VIEW v_audit_summary AS
SELECT 
    DATE(timestamp) as fecha,
    usuario,
    accion,
    COUNT(*) as total_operaciones,
    SUM(IF(resultado = 'EXITOSO', 1, 0)) as operaciones_exitosas,
    SUM(IF(resultado = 'ERROR', 1, 0)) as operaciones_error
FROM audit_log
GROUP BY DATE(timestamp), usuario, accion
ORDER BY fecha DESC;

-- Para ejecutar: SET GLOBAL event_scheduler = ON;
-- Limpiar datos antiguos (30+ días) semanalmente

CREATE EVENT IF NOT EXISTS cleanup_old_audit_logs
ON SCHEDULE EVERY 1 WEEK STARTS NOW()
DO
    DELETE FROM audit_log WHERE timestamp < DATE_SUB(NOW(), INTERVAL 30 DAY);

CREATE EVENT IF NOT EXISTS cleanup_old_sessions
ON SCHEDULE EVERY 1 DAY STARTS NOW()
DO
    DELETE FROM user_session WHERE fecha_expiracion < NOW() AND es_activa = FALSE;

CREATE EVENT IF NOT EXISTS cleanup_old_failed_logins
ON SCHEDULE EVERY 1 WEEK STARTS NOW()
DO
    DELETE FROM failed_login_attempt WHERE timestamp < DATE_SUB(NOW(), INTERVAL 7 DAY);
