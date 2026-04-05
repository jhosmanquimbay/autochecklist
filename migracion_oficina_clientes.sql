ALTER TABLE inventario
    ADD COLUMN IF NOT EXISTS cantidad_disponible INT NOT NULL DEFAULT 1;

ALTER TABLE pedido
    ADD COLUMN IF NOT EXISTS stock_descontado TINYINT(1) NOT NULL DEFAULT 0;

UPDATE inventario
SET cantidad_disponible = 1
WHERE cantidad_disponible IS NULL OR cantidad_disponible < 0;

UPDATE pedido
SET stock_descontado = 0
WHERE stock_descontado IS NULL;