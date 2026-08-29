package com.alphabike.backend.trabajo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrabajoRealizadoRepository extends JpaRepository<TrabajoRealizado, String> {
    List<TrabajoRealizado> findByDestacado(Boolean destacado);
    List<TrabajoRealizado> findByCitaId(String citaId);
}