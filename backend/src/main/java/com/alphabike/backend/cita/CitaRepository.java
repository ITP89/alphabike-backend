package com.alphabike.backend.cita;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, String> {

    // Lock only the parent row: eager outer joins are incompatible with FOR UPDATE
    // on nullable relations in PostgreSQL. Lazy relations are read in the service transaction.
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Cita c where c.id = :id")
    Optional<Cita> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") String id);

    @EntityGraph(attributePaths = {"cliente", "encargado", "servicio"})
    List<Cita> findByClienteId(String clienteId);

    @EntityGraph(attributePaths = {"cliente", "encargado", "servicio"})
    List<Cita> findByEncargadoId(String encargadoId);

    @EntityGraph(attributePaths = {"cliente", "encargado", "servicio"})
    List<Cita> findByEstado(Cita.Estado estado);

    @Override
    @EntityGraph(attributePaths = {"cliente", "encargado", "servicio"})
    Optional<Cita> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"cliente", "encargado", "servicio"})
    List<Cita> findAll();
}
