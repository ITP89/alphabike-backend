-- ============================================================
-- V5 - PRODUCTOS
-- Catálogo de productos de la tienda.
-- Depende de: tienda.categorias
-- Estados: ACTIVO | DESCONTINUADO
-- ============================================================

CREATE TABLE IF NOT EXISTS productos (
    id           VARCHAR(255)   PRIMARY KEY,
    categoria_id VARCHAR(255)   NOT NULL
                     REFERENCES categorias(id),
    nombre       VARCHAR(255)   NOT NULL,
    descripcion  VARCHAR(255),
    marca        VARCHAR(255)   NOT NULL,
    precio       NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
    stock        INTEGER        NOT NULL CHECK (stock >= 0),
    imagen_url   VARCHAR(255),
    estado       VARCHAR(255)   NOT NULL
                     CHECK (estado IN ('ACTIVO', 'DESCONTINUADO'))
);

-- Índice de búsqueda por categoría
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);

