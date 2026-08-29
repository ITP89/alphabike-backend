package com.alphabike.backend.pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, String> {
    List<Pago> findByReferenciaId(String referenciaId);
    List<Pago> findByEstado(Pago.Estado estado);
}