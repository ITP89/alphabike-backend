-- ============================================================
-- V4 - SCHEMA TALLER & SERVICIOS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS taller;

CREATE TABLE IF NOT EXISTS servicios (
    id           VARCHAR(255)   PRIMARY KEY,
    nombre       VARCHAR(255)   NOT NULL UNIQUE,
    descripcion  VARCHAR(255),
    precio_base  NUMERIC(10, 2) NOT NULL CHECK (precio_base > 0),
    duracion_min INTEGER        NOT NULL CHECK (duracion_min > 0)
);

