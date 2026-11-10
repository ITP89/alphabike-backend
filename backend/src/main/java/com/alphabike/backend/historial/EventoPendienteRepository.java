package com.alphabike.backend.historial;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoPendienteRepository extends JpaRepository<EventoPendiente, String> {
    List<EventoPendiente> findTop50ByOrderByOcurridoEnAscIdAsc();
}
