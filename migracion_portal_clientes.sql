SET @db_name = DATABASE();

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'cedula'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN cedula VARCHAR(20) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'telefono'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN telefono VARCHAR(20) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'direccion'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN direccion VARCHAR(255) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'ciudad'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN ciudad VARCHAR(100) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'barrio'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN barrio VARCHAR(100) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'localidad'
    ),
    'SELECT 1',
    'ALTER TABLE usuario ADD COLUMN localidad VARCHAR(100) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'uk_usuario_cedula'
    ),
    'SELECT 1',
    'CREATE UNIQUE INDEX uk_usuario_cedula ON usuario (cedula)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(30) NULL,
    cedula VARCHAR(20) NULL UNIQUE,
    ciudad VARCHAR(120) NULL,
    barrio VARCHAR(100) NULL,
    localidad VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    interes_vehiculo VARCHAR(255) NULL,
    presupuesto DOUBLE NULL,
    estado VARCHAR(60) NULL,
    fecha_registro DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_interaccion DATETIME NULL,
    notas TEXT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'clientes' AND COLUMN_NAME = 'barrio'
    ),
    'SELECT 1',
    'ALTER TABLE clientes ADD COLUMN barrio VARCHAR(100) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'clientes' AND COLUMN_NAME = 'localidad'
    ),
    'SELECT 1',
    'ALTER TABLE clientes ADD COLUMN localidad VARCHAR(100) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO rol (nombre)
SELECT 'CLIENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM rol WHERE UPPER(nombre) IN ('CLIENTE', 'ROLE_CLIENTE')
);

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pedido' AND COLUMN_NAME = 'cliente_usuario_id'
    ),
    'SELECT 1',
    'ALTER TABLE pedido ADD COLUMN cliente_usuario_id INT(11) NULL'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE pedido MODIFY COLUMN cliente_usuario_id INT(11) NULL;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'pedido' AND INDEX_NAME = 'idx_pedido_cliente_usuario'
    ),
    'SELECT 1',
    'CREATE INDEX idx_pedido_cliente_usuario ON pedido (cliente_usuario_id)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    EXISTS(
        SELECT 1 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = @db_name AND CONSTRAINT_NAME = 'fk_pedido_cliente_usuario'
    ),
    'SELECT 1',
    'ALTER TABLE pedido ADD CONSTRAINT fk_pedido_cliente_usuario FOREIGN KEY (cliente_usuario_id) REFERENCES usuario(id_usuario)'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS favorito (
    id_favorito BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT(11) NOT NULL,
    vehiculo_id INT(11) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_favorito_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(idVehiculo),
    CONSTRAINT uq_favorito_usuario_vehiculo UNIQUE (usuario_id, vehiculo_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;