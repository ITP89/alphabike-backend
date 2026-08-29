-- ============================================================
-- V12 - SEPARACIÓN EN ESQUEMAS EN SUPABASE
-- Crea esquemas por dominio y mueve tablas de public a su esquema
-- ============================================================

CREATE SCHEMA IF NOT EXISTS auth_app;
CREATE SCHEMA IF NOT EXISTS tienda;
CREATE SCHEMA IF NOT EXISTS taller;
CREATE SCHEMA IF NOT EXISTS pagos;

-- Mover usuarios
ALTER TABLE IF EXISTS public.usuarios SET SCHEMA auth_app;

-- Mover tienda
ALTER TABLE IF EXISTS public.categorias SET SCHEMA tienda;
ALTER TABLE IF EXISTS public.productos SET SCHEMA tienda;
ALTER TABLE IF EXISTS public.pedidos SET SCHEMA tienda;
ALTER TABLE IF EXISTS public.detalle_pedidos SET SCHEMA tienda;

-- Mover taller
ALTER TABLE IF EXISTS public.servicios SET SCHEMA taller;
ALTER TABLE IF EXISTS public.citas SET SCHEMA taller;
ALTER TABLE IF EXISTS public.cotizaciones SET SCHEMA taller;
ALTER TABLE IF EXISTS public.trabajos_realizados SET SCHEMA taller;

-- Mover pagos
ALTER TABLE IF EXISTS public.pagos SET SCHEMA pagos;
