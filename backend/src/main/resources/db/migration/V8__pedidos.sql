-- ============================================================
-- V8 - PEDIDOS
-- Órdenes de compra online de los clientes.
-- Depende de: auth_app.usuarios
-- Estados: PENDIENTE | PAGADO | EN_PREPARACION | LISTO_PARA_RECOJO
--          | EN_CAMINO | ENVIADO | ENTREGADO | CANCELADO
-- Tipo entrega: RECOJO_TIENDA | DELIVERY_LIMA | ENVIO_PROVINCIA
-- ============================================================

CREATE TABLE IF NOT EXISTS pedidos (
    id                VARCHAR(255)   PRIMARY KEY,
    cliente_id        VARCHAR(255)   NOT NULL
                          REFERENCES usuarios(id),
    fecha             TIMESTAMP      NOT NULL,
    estado            VARCHAR(255)   NOT NULL
                          CHECK (estado IN (
                              'PENDIENTE',
                              'PAGADO',
                              'EN_PREPARACION',
                              'LISTO_PARA_RECOJO',
                              'EN_CAMINO',
                              'ENVIADO',
                              'ENTREGADO',
                              'CANCELADO'
                          )),
    tipo_entrega      VARCHAR(255)   NOT NULL
                          CHECK (tipo_entrega IN (
                              'RECOJO_TIENDA',
                              'DELIVERY_LIMA',
                              'ENVIO_PROVINCIA'
                          )),
    direccion_entrega VARCHAR(255),
    costo_envio       NUMERIC(10, 2),
    numero_seguimiento VARCHAR(255),
    total             NUMERIC(10, 2) NOT NULL CHECK (total >= 0)
);

-- Índice para consultar pedidos por cliente
CREATE INDEX IF NOT EXISTS idx_pedidos_cliente ON pedidos(cliente_id);

