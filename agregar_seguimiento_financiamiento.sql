ALTER TABLE solicitud_financiamiento
    ADD COLUMN IF NOT EXISTS estado_documental VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    ADD COLUMN IF NOT EXISTS etapa_proceso VARCHAR(40) NOT NULL DEFAULT 'RADICACION_PENDIENTE',
    ADD COLUMN IF NOT EXISTS negocio_creado BIT NOT NULL DEFAULT b'0',
    ADD COLUMN IF NOT EXISTS fecha_negocio_creado DATETIME NULL,
    ADD COLUMN IF NOT EXISTS entidad_financiera VARCHAR(120) NULL,
    ADD COLUMN IF NOT EXISTS observaciones_seguimiento TEXT NULL,
    ADD COLUMN IF NOT EXISTS fecha_ultima_gestion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_desembolso_programada DATE NULL,
    ADD COLUMN IF NOT EXISTS fecha_desembolso_real DATE NULL,
    ADD COLUMN IF NOT EXISTS monto_desembolsado DOUBLE NULL;

CREATE INDEX IF NOT EXISTS idx_solicitud_financiamiento_fecha_gestion
    ON solicitud_financiamiento(fecha_ultima_gestion);