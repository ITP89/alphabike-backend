-- ============================================================
-- V6 - CITAS
-- Agendamiento de citas de mantenimiento en el taller.
-- Depende de: auth_app.usuarios, taller.servicios
-- Estados: PENDIENTE | EN_PROCESO | COMPLETADO | CANCELADO
-- ============================================================

CREATE TABLE IF NOT EXISTS citas (
    id               VARCHAR(255) PRIMARY KEY,
    cliente_id       VARCHAR(255) NOT NULL
                         REFERENCES usuarios(id),
    encargado_id     VARCHAR(255)
                         REFERENCES usuarios(id),
    servicio_id      VARCHAR(255) NOT NULL
                         REFERENCES servicios(id),
    fecha            DATE         NOT NULL,
    hora             TIME         NOT NULL,
    estado           VARCHAR(255) NOT NULL
                         CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'COMPLETADO', 'CANCELADO')),
    bici_descripcion VARCHAR(255),
    observaciones    VARCHAR(255)
);

-- Índices para consultas frecuentes por cliente y encargado
CREATE INDEX IF NOT EXISTS idx_citas_cliente   ON citas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_citas_encargado ON citas(encargado_id);

