package com.alphabike.backend.historial;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.historial.enabled", havingValue = "true", matchIfMissing = true)
public class HistorialMongoService {
    private final MongoTemplate mongo;

    public void guardar(EventoPendiente pendiente) {
        // Stable SQL event ID makes retries and concurrent workers idempotent.
        mongo.save(EventoCita.from(pendiente));
    }

    public List<EventoCita> listar(String citaId, int pagina) {
        Query query = Query.query(Criteria.where("citaId").is(citaId))
                .with(Sort.by(Sort.Direction.DESC, "ocurridoEn", "_id"))
                .skip((long) pagina * 50).limit(50);
        return mongo.find(query, EventoCita.class);
    }
}
