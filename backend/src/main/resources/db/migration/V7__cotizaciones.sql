-- ============================================================
-- V7 - COTIZACIONES
-- Presupuestos generados a partir de una cita completada.
-- Depende de: taller.citas
-- Estados: PENDIENTE | ACEPTADA | RECHAZADA
-- ============================================================

CREATE TABLE IF NOT EXISTS cotizaciones (
    id            VARCHAR(255)   PRIMARY KEY,
    cita_id       VARCHAR(255)   NOT NULL UNIQUE
                      REFERENCES citas(id),
    descripcion   VARCHAR(255)   NOT NULL,
    monto         NUMERIC(10, 2) NOT NULL CHECK (monto > 0),
    fecha_emision TIMESTAMP      NOT NULL,
    estado        VARCHAR(255)   NOT NULL
                      CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA'))
);

