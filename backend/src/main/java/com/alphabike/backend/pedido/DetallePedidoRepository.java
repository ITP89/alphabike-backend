package com.alphabike.backend.pedido;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, String> {

    @EntityGraph(attributePaths = "producto")
    List<DetallePedido> findByPedidoId(String pedidoId);
}