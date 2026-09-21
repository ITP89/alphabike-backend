-- ============================================================
-- V22 - PROVEEDORES
-- Gestion administrativa de proveedores y asociacion opcional a productos.
-- ============================================================

CREATE TABLE IF NOT EXISTS tienda.proveedores (
    id                  VARCHAR(255) PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    ruc                 VARCHAR(20)  NOT NULL UNIQUE,
    telefono            VARCHAR(50)  NOT NULL,
    email               VARCHAR(255) NOT NULL,
    direccion           VARCHAR(255) NOT NULL,
    contacto_principal  VARCHAR(255),
    estado              VARCHAR(255) NOT NULL
                            CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

ALTER TABLE tienda.productos
    ADD COLUMN IF NOT EXISTS proveedor_id VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_productos_proveedor'
    ) THEN
        ALTER TABLE tienda.productos
            ADD CONSTRAINT fk_productos_proveedor
            FOREIGN KEY (proveedor_id)
            REFERENCES tienda.proveedores(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_productos_proveedor ON tienda.productos(proveedor_id);
