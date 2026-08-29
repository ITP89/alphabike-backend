package com.alphabike.backend.cita;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, String> {

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