package com.alphabike.backend.cotizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, String> {
    @org.springframework.data.jpa.repository.Query("select c.cita.id from Cotizacion c where c.id = :id")
    Optional<String> findCitaIdById(@org.springframework.data.repository.query.Param("id") String id);
    Optional<Cotizacion> findByCitaId(String citaId);
}
