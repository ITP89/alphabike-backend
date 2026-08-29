-- ============================================================
-- V1 - DROP ALL SCHEMAS AND TABLES
-- Clean reset for PostgreSQL multi-schema architecture in Supabase
-- ============================================================

DROP SCHEMA IF EXISTS pagos CASCADE;
DROP SCHEMA IF EXISTS taller CASCADE;
DROP SCHEMA IF EXISTS tienda CASCADE;
DROP SCHEMA IF EXISTS auth_app CASCADE;

-- Legacy tables drop if any exist in public
DROP TABLE IF EXISTS trabajos_realizados CASCADE;
DROP TABLE IF EXISTS pagos CASCADE;
DROP TABLE IF EXISTS detalle_pedidos CASCADE;
DROP TABLE IF EXISTS pedidos CASCADE;
DROP TABLE IF EXISTS cotizaciones CASCADE;
DROP TABLE IF EXISTS citas CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS servicios CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

