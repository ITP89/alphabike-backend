-- ============================================================
-- V2 - SCHEMA AUTH_APP & USUARIOS
-- ============================================================

CREATE SCHEMA IF NOT EXISTS auth_app;

CREATE TABLE IF NOT EXISTS usuarios (
    id             VARCHAR(255) PRIMARY KEY,
    nombre         VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    telefono       VARCHAR(255) NOT NULL,
    rol            VARCHAR(255) NOT NULL
                       CHECK (rol IN ('ADMIN', 'ENCARGADO', 'CLIENTE')),
    estado         VARCHAR(255) NOT NULL
                       CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    fecha_registro TIMESTAMP   NOT NULL
);

