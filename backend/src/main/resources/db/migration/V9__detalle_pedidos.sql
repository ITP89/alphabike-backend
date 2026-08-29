-- ============================================================
-- V9 - DETALLE_PEDIDOS
-- Líneas de cada pedido (productos y cantidades).
-- Depende de: tienda.pedidos, tienda.productos
-- precio_lista  = precio publicado al momento de la compra
-- precio_acordado = precio final aplicado (puede diferir por descuento)
-- ============================================================

CREATE TABLE IF NOT EXISTS detalle_pedidos (
    id              VARCHAR(255)   PRIMARY KEY,
    pedido_id       VARCHAR(255)   NOT NULL
                        REFERENCES pedidos(id),
    producto_id     VARCHAR(255)   NOT NULL
                        REFERENCES productos(id),
    cantidad        INTEGER        NOT NULL CHECK (cantidad > 0),
    precio_lista    NUMERIC(10, 2) NOT NULL CHECK (precio_lista > 0),
    precio_acordado NUMERIC(10, 2) NOT NULL CHECK (precio_acordado > 0),
    subtotal        NUMERIC(10, 2) NOT NULL CHECK (subtotal >= 0)
);

-- Índice para obtener las líneas de un pedido
CREATE INDEX IF NOT EXISTS idx_detalle_pedidos_pedido ON detalle_pedidos(pedido_id);

