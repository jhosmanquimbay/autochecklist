ALTER TABLE password_reset_token
    DROP INDEX token_hash;

CREATE INDEX idx_password_reset_usuario_fecha
    ON password_reset_token (usuario_id, fecha_uso, fecha_solicitud);