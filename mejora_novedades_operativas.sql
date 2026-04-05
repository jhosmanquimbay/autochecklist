ALTER TABLE novedades ADD COLUMN IF NOT EXISTS VehiculoChasis VARCHAR(255) NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS VehiculoReferencia VARCHAR(255) NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS OrigenReporte VARCHAR(100) NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS Prioridad VARCHAR(50) NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS AplicaGarantia BIT NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS AccionRequerida TEXT NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS ObservacionGerente TEXT NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS FechaReporte DATETIME NULL;
ALTER TABLE novedades ADD COLUMN IF NOT EXISTS FechaGestion DATETIME NULL;

UPDATE novedades
SET OrigenReporte = COALESCE(OrigenReporte, 'recepcion_tienda'),
    Prioridad = COALESCE(Prioridad, 'media'),
    Estado = CASE
        WHEN Estado IS NULL OR TRIM(Estado) = '' THEN 'pendiente'
        WHEN LOWER(TRIM(Estado)) IN ('en revisión', 'en revision', 'revision') THEN 'en_revision'
        WHEN LOWER(TRIM(Estado)) IN ('garantia', 'gestión garantía', 'gestion garantia') THEN 'gestion_garantia'
        WHEN LOWER(TRIM(Estado)) IN ('resuelto', 'cerrado') THEN 'resuelto'
        ELSE 'pendiente'
    END,
    AplicaGarantia = COALESCE(AplicaGarantia, 0),
    FechaReporte = COALESCE(FechaReporte, NOW()),
    FechaGestion = COALESCE(FechaGestion, NOW());