-- ============================================================
-- V21 - LIMITE DE REGATEO POR PRODUCTO
-- Permite definir el precio minimo autorizado en venta presencial.
-- ============================================================

ALTER TABLE tienda.productos
    ADD COLUMN IF NOT EXISTS precio_minimo_venta NUMERIC(10, 2);

UPDATE tienda.productos
SET precio_minimo_venta = ROUND(precio * 0.90, 2)
WHERE precio_minimo_venta IS NULL;

ALTER TABLE tienda.productos
    ALTER COLUMN precio_minimo_venta SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_productos_precio_minimo_venta'
    ) THEN
        ALTER TABLE tienda.productos
            ADD CONSTRAINT chk_productos_precio_minimo_venta
            CHECK (precio_minimo_venta > 0 AND precio_minimo_venta <= precio);
    END IF;
END $$;
