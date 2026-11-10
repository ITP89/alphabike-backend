-- Durable bridge to MongoDB. No cross-database foreign keys or distributed transaction.
CREATE TABLE taller.historial_outbox (
    id VARCHAR(255) PRIMARY KEY,
    cita_id VARCHAR(255) NOT NULL,
    estado VARCHAR(255) NOT NULL,
    ocurrido_en TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_historial_outbox_fecha ON taller.historial_outbox (ocurrido_en, id);

-- Initial snapshot for existing appointments, not a reconstruction of past changes.
INSERT INTO taller.historial_outbox (id, cita_id, estado, ocurrido_en)
SELECT 'snapshot-' || id, id, estado, CURRENT_TIMESTAMP FROM taller.citas;
