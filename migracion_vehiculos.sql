-- ============================================================
-- MIGRACIÓN: Agregar nuevas columnas a tabla 'vehiculo'
-- ============================================================

-- Agregar columnas faltantes (una por una para evitar errores)
ALTER TABLE vehiculo ADD COLUMN Marca VARCHAR(100) DEFAULT 'Desconocida';
ALTER TABLE vehiculo ADD COLUMN Anio INT DEFAULT 2024;
ALTER TABLE vehiculo ADD COLUMN Precio DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE vehiculo ADD COLUMN Cilindrada VARCHAR(50) DEFAULT 'N/A';
ALTER TABLE vehiculo ADD COLUMN TipoCombustible VARCHAR(50) DEFAULT 'Gasolina';
ALTER TABLE vehiculo ADD COLUMN Transmision VARCHAR(50) DEFAULT 'Automática';
ALTER TABLE vehiculo ADD COLUMN Descripcion LONGTEXT;
ALTER TABLE vehiculo ADD COLUMN ImagenUrl VARCHAR(255);
ALTER TABLE vehiculo ADD COLUMN FechaCreacion DATETIME DEFAULT NOW();

-- Verificar estructura final
DESCRIBE vehiculo;
