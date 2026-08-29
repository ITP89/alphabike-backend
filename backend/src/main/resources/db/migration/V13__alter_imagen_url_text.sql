-- ============================================================
-- V13 - ALTER COLUMN imagen_url TO TEXT IN tienda.productos
-- Permite almacenar URLs de imagen largas y Base64 Data URLs
-- ============================================================

ALTER TABLE tienda.productos ALTER COLUMN imagen_url TYPE TEXT;
