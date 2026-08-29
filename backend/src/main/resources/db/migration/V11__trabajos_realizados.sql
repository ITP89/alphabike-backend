-- ============================================================
-- V11 - TRABAJOS_REALIZADOS
-- Registra los trabajos ejecutados en una cita de taller.
-- También funciona como galería (imagen antes/después).
-- Depende de: taller.citas
-- destacado = true  → aparece en la galería pública del taller
-- ============================================================

CREATE TABLE IF NOT EXISTS trabajos_realizados (
    id                VARCHAR(255) PRIMARY KEY,
    cita_id           VARCHAR(255)
                          REFERENCES citas(id),
    titulo            VARCHAR(255) NOT NULL,
    descripcion       VARCHAR(255),
    imagen_antes_url  VARCHAR(255),
    imagen_despues_url VARCHAR(255),
    fecha             DATE         NOT NULL,
    destacado         BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Índice para filtrar trabajos por cita
CREATE INDEX IF NOT EXISTS idx_trabajos_cita      ON trabajos_realizados(cita_id);

-- Índice para la galería pública (trabajos destacados)
CREATE INDEX IF NOT EXISTS idx_trabajos_destacado ON trabajos_realizados(destacado);

