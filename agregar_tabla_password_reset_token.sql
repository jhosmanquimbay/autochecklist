CREATE TABLE IF NOT EXISTS password_reset_token (
    id_password_reset_token INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATETIME NOT NULL,
    fecha_uso DATETIME NULL,

    CONSTRAINT fk_password_reset_token_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario)
        ON DELETE CASCADE,

CREATE INDEX idx_password_reset_usuario_fecha
    ON password_reset_token (usuario_id, fecha_uso, fecha_solicitud);

    INDEX idx_password_reset_token_usuario (usuario_id),
    INDEX idx_password_reset_token_expiracion (fecha_expiracion),
    INDEX idx_password_reset_token_uso (fecha_uso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;