-- ============================================================
-- MIGRACION: flujo de publicacion de vehiculos
-- Gerencia carga la informacion y admin publica.
-- ============================================================

SET @tenia_estado_publicacion := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'vehiculo'
      AND COLUMN_NAME = 'estado_publicacion'
);

ALTER TABLE vehiculo
    ADD COLUMN IF NOT EXISTS estado_publicacion VARCHAR(30) NOT NULL DEFAULT 'BORRADOR' AFTER activo;

ALTER TABLE vehiculo
    ADD COLUMN IF NOT EXISTS fecha_publicacion DATETIME NULL AFTER estado_publicacion;

UPDATE vehiculo
SET estado_publicacion = CASE
        WHEN COALESCE(activo, 0) = 1 THEN 'PUBLICADO'
        ELSE 'BORRADOR'
    END,
    fecha_publicacion = CASE
        WHEN COALESCE(activo, 0) = 1 THEN COALESCE(fecha_publicacion, FechaCreacion, NOW())
        ELSE NULL
    END
WHERE @tenia_estado_publicacion = 0;

SELECT idVehiculo, Marca, Modelo, activo, estado_publicacion, fecha_publicacion
FROM vehiculo
ORDER BY idVehiculo DESC
LIMIT 20;