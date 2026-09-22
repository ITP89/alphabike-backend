-- ============================================================
-- V23 - AGREGAR TARJETA Y CAMPOS DE PASARELA A PAGOS
-- ============================================================

-- Actualizar constraint de método de pago para admitir 'TARJETA'
ALTER TABLE pagos.pagos DROP CONSTRAINT IF EXISTS pagos_metodo_pago_check;
ALTER TABLE pagos.pagos ADD CONSTRAINT pagos_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'YAPE', 'PLIN', 'TRANSFERENCIA', 'TARJETA'));

-- Agregar columnas para auditoría de pasarela de pago (Culqi / MercadoPago / Sandbox)
ALTER TABLE pagos.pagos ADD COLUMN IF NOT EXISTS codigo_autorizacion VARCHAR(255);
ALTER TABLE pagos.pagos ADD COLUMN IF NOT EXISTS transaccion_id VARCHAR(255);
ALTER TABLE pagos.pagos ADD COLUMN IF NOT EXISTS tarjeta_marca VARCHAR(50);
ALTER TABLE pagos.pagos ADD COLUMN IF NOT EXISTS tarjeta_ultimos4 VARCHAR(10);
