package com.alphabike.backend.realtime;

/** Immutable event captured inside the transaction; no lazy entities escape it. */
public record CitaChanged(String citaId, String clienteEmail, String estado) {}
