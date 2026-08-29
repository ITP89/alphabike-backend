-- ============================================================
-- V3 - SCHEMA TIENDA & CATEGORIAS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS tienda;

CREATE TABLE IF NOT EXISTS categorias (
    id          VARCHAR(255) PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

