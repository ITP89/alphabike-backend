ALTER TABLE tienda.pedidos ADD COLUMN mercado_pago_order_id VARCHAR(255) UNIQUE;
ALTER TABLE pagos.pagos DROP CONSTRAINT IF EXISTS pagos_metodo_pago_check;
ALTER TABLE pagos.pagos ADD CONSTRAINT pagos_metodo_pago_check
    CHECK (metodo_pago IN ('EFECTIVO', 'YAPE', 'PLIN', 'TRANSFERENCIA', 'TARJETA', 'MERCADO_PAGO'));
ALTER TABLE tienda.pedidos ADD COLUMN mercado_pago_checkout_url VARCHAR(2048);
