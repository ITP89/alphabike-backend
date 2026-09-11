-- ============================================================
-- V1 - SAFE INITIAL BASELINE
-- Never drop schemas or tables from a normal Flyway migration.
-- Destructive resets must be explicit local-only operations.
-- ============================================================

CREATE SCHEMA IF NOT EXISTS auth_app;
CREATE SCHEMA IF NOT EXISTS tienda;
CREATE SCHEMA IF NOT EXISTS taller;
CREATE SCHEMA IF NOT EXISTS pagos;

