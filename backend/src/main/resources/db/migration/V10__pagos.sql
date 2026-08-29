-- ============================================================
-- V10 - SCHEMA PAGOS & PAGOS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS pagos;

CREATE TABLE IF NOT EXISTS pagos (
    id              VARCHAR(255)   PRIMARY KEY,
    referencia_tipo VARCHAR(255)   NOT NULL
                        CHECK (referencia_tipo IN ('PEDIDO', 'COTIZACION')),
    referencia_id   VARCHAR(255)   NOT NULL,
    monto           NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    metodo_pago     VARCHAR(255)   NOT NULL
                        CHECK (metodo_pago IN ('EFECTIVO', 'YAPE', 'PLIN', 'TRANSFERENCIA')),
    estado          VARCHAR(255)   NOT NULL
                        CHECK (estado IN ('PENDIENTE', 'PAGADO')),
    fecha           TIMESTAMP      NOT NULL
);

-- Índice compuesto para buscar pagos por referencia
CREATE INDEX IF NOT EXISTS idx_pagos_referencia ON pagos(referencia_tipo, referencia_id);

