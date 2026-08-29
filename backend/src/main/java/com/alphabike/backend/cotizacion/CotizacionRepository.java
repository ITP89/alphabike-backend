package com.alphabike.backend.cotizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, String> {
    Optional<Cotizacion> findByCitaId(String citaId);
}