package com.alphabike.backend.proveedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, String> {
    Optional<Proveedor> findByRuc(String ruc);
}
