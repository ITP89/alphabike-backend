-- ============================================================
-- V16 - ELIMINAR TIJA DUPLICADA DESCONTINUADA
-- ============================================================

-- Si hay referencias en detalle_pedidos a la Tija descontinuada (S/ 55.00), apuntarlas a la Tija Telescópica Fox
UPDATE tienda.detalle_pedidos
SET producto_id = '8cdb7dc1-d660-4102-8cef-e4b6a290a125'
WHERE producto_id IN (
  SELECT id FROM tienda.productos WHERE nombre = 'Tija' AND precio = 55.00
);

-- Eliminar la Tija vieja descontinuada de la BD
DELETE FROM tienda.productos
WHERE (nombre = 'Tija' AND precio = 55.00) OR estado = 'DESCONTINUADO';

-- Garantizar que la categoría de Cadena sea Transmisión
UPDATE tienda.productos
SET categoria_id = (SELECT id FROM tienda.categorias WHERE nombre = 'Transmisión' LIMIT 1)
WHERE nombre LIKE '%Cadena%';
