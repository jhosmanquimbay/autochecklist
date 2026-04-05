-- Schema SQL (temporary dev migration)
-- Crea tablas en orden para evitar errores de clave foránea
CREATE TABLE IF NOT EXISTS rol (
  idRol BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS usuario (
  id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
  Nombre VARCHAR(100) NOT NULL,
  Correo VARCHAR(150) NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  Rol_idRol BIGINT,
  CONSTRAINT FK_usuario_rol FOREIGN KEY (Rol_idRol) REFERENCES rol(idRol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mensaje (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  remitente_id BIGINT,
  destinatario_id BIGINT,
  asunto VARCHAR(255),
  contenido TEXT,
  fechaEnvio DATETIME,
  leido BOOLEAN DEFAULT FALSE,
  carpeta VARCHAR(50) DEFAULT 'inbox',
  eliminado BOOLEAN DEFAULT FALSE,
  id_padre BIGINT,
  CONSTRAINT FK_mensaje_remitente FOREIGN KEY (remitente_id) REFERENCES usuario(id_usuario),
  CONSTRAINT FK_mensaje_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS adjunto (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombreArchivo VARCHAR(255),
  rutaArchivo VARCHAR(1024),
  mensaje_id BIGINT,
  CONSTRAINT FK_adjunto_mensaje FOREIGN KEY (mensaje_id) REFERENCES mensaje(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Fin schema.sql

-- Asegurar columnas necesarias para la entidad `Mensaje` cuando se actualiza desde código
ALTER TABLE mensaje ADD COLUMN IF NOT EXISTS destinatarioExterno VARCHAR(255) DEFAULT NULL;
ALTER TABLE mensaje ADD COLUMN IF NOT EXISTS idPadre BIGINT DEFAULT NULL;

-- Nota: `id_padre` puede existir según versiones anteriores; `idPadre` se añade para compatibilidad
