package com.alphabike.backend.producto;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByCategoriaId(String categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByEstado(Producto.Estado estado);

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAll();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Producto> findById(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p join fetch p.categoria where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") String id);
}
