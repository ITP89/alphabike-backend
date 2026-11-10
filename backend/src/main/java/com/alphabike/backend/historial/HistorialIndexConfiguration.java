package com.alphabike.backend.historial;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor @Slf4j
@ConditionalOnProperty(name = "app.historial.enabled", havingValue = "true", matchIfMissing = true)
public class HistorialIndexConfiguration {
    private final MongoTemplate mongo;
    private volatile boolean listo;

    @Scheduled(fixedDelay = 60000, initialDelay = 1000)
    public void asegurarIndice() {
        if (listo) return;
        try {
            mongo.indexOps(EventoCita.class).createIndex(new Index()
                    .on("citaId", Sort.Direction.ASC).on("ocurridoEn", Sort.Direction.DESC)
                    .on("_id", Sort.Direction.DESC).named("cita_historial_fecha"));
            listo = true;
        } catch (RuntimeException ex) {
            log.warn("Indice del historial pendiente; se reintentara ({})", ex.getClass().getSimpleName());
        }
    }
}
