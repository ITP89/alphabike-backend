package com.alphabike.backend.historial;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/** Immutable document; excludes customer email, payment and bicycle details. */
@Document(collection = "cita_eventos")
public record EventoCita(@Id String id, String citaId, String estado, Instant ocurridoEn) {
    public static EventoCita from(EventoPendiente e) {
        return new EventoCita(e.getId(), e.getCitaId(), e.getEstado(), e.getOcurridoEn());
    }
}
