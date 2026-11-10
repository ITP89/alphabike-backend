ALTER TABLE tienda.pedidos ADD COLUMN operacion_id VARCHAR(255);
ALTER TABLE tienda.pedidos ADD COLUMN solicitud_hash VARCHAR(64);
CREATE UNIQUE INDEX uq_pedidos_operacion ON tienda.pedidos (operacion_id);
