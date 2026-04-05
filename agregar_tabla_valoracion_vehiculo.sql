CREATE TABLE IF NOT EXISTS valoracion_vehiculo (
    id_valoracion_vehiculo BIGINT NOT NULL AUTO_INCREMENT,
    vehiculo_id INT NOT NULL,
    inventario_id INT NULL,
    soat_vencimiento DATE NULL,
    tecnicomecanica_vencimiento DATE NULL,
    tarjeta_propiedad_ok TINYINT(1) NOT NULL DEFAULT 0,
    impuestos_al_dia TINYINT(1) NOT NULL DEFAULT 0,
    prenda_activa TINYINT(1) NOT NULL DEFAULT 0,
    precio_objetivo_manual DOUBLE NULL,
    observaciones TEXT NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_valoracion_vehiculo),
    UNIQUE KEY uk_valoracion_vehiculo (vehiculo_id),
    KEY idx_valoracion_inventario (inventario_id),
    CONSTRAINT fk_valoracion_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(idVehiculo),
    CONSTRAINT fk_valoracion_inventario FOREIGN KEY (inventario_id) REFERENCES inventario(IdInventario)
) ENGINE=InnoDB;