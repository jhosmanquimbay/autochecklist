-- ============================================================
-- MIGRACION: contabilidad detallada por venta
-- Conecta pedido, vehiculo e inventario para calcular utilidad,
-- comisión y reinversión por operación.
-- ============================================================

CREATE TABLE IF NOT EXISTS contabilidad_venta (
    id_contabilidad_venta BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    vehiculo_id INT NULL,
    inventario_id INT NULL,
    precio_publicado_snapshot DECIMAL(15,2) NULL DEFAULT 0.00,
    precio_venta_final DECIMAL(15,2) NULL DEFAULT 0.00,
    costo_base DECIMAL(15,2) NULL DEFAULT 0.00,
    costo_acondicionamiento DECIMAL(15,2) NULL DEFAULT 0.00,
    costo_traslado DECIMAL(15,2) NULL DEFAULT 0.00,
    costo_administrativo DECIMAL(15,2) NULL DEFAULT 0.00,
    gasto_publicacion DECIMAL(15,2) NULL DEFAULT 0.00,
    gastos_cierre DECIMAL(15,2) NULL DEFAULT 0.00,
    porcentaje_comision DECIMAL(5,2) NULL DEFAULT 8.00,
    porcentaje_reinversion DECIMAL(5,2) NULL DEFAULT 60.00,
    notas LONGTEXT NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id_contabilidad_venta),
    UNIQUE KEY uk_contabilidad_venta_pedido (pedido_id),
    CONSTRAINT fk_contabilidad_venta_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id_pedido),
    CONSTRAINT fk_contabilidad_venta_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(idVehiculo),
    CONSTRAINT fk_contabilidad_venta_inventario FOREIGN KEY (inventario_id) REFERENCES inventario(IdInventario)
) ENGINE=InnoDB;