package com.alphabike.backend.pedido;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, String> {
    Optional<Pedido> findByOperacionId(String operacionId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") String id);


    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<Pedido> findByClienteId(String clienteId);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<Pedido> findByEstado(Pedido.Estado estado);

    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<Pedido> findByTipoEntrega(Pedido.TipoEntrega tipoEntrega);

    @Override
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    Optional<Pedido> findById(String id);

    @Override
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.producto"})
    List<Pedido> findAll();
}