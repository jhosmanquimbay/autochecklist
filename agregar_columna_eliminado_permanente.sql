-- Script para agregar la columna eliminado_permanente a la tabla mensaje
-- Ejecutar este script en MySQL para habilitar la funcionalidad de "Vaciar Papelera"

USE auto;

-- Agregar columna eliminado_permanente después de la columna eliminado
ALTER TABLE mensaje 
ADD COLUMN eliminado_permanente BOOLEAN DEFAULT FALSE AFTER eliminado;

-- Reparar esquemas legacy donde existe la columna camelCase creada sin default.
-- Esa columna no la usa la entidad JPA actual, pero si queda NOT NULL sin valor por
-- defecto rompe cualquier INSERT nuevo en la tabla mensaje.
ALTER TABLE mensaje
MODIFY COLUMN eliminadoPermanente BOOLEAN NOT NULL DEFAULT FALSE;

-- Verificar que la columna se agregó correctamente
DESCRIBE mensaje;

-- Opcional: Ver los primeros registros para confirmar
SELECT id, asunto, eliminado, eliminado_permanente, eliminadoPermanente FROM mensaje LIMIT 5;
