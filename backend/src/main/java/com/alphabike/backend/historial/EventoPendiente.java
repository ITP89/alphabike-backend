package com.alphabike.backend.historial;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/** SQL outbox: committed atomically with the appointment mutation. */
@Entity
@Table(name = "historial_outbox", schema = "taller")
@Getter @Setter @NoArgsConstructor
public class EventoPendiente {
    @Id private String id;
    @Column(name = "cita_id", nullable = false) private String citaId;
    @Column(nullable = false) private String estado;
    @Column(name = "ocurrido_en", nullable = false) private Instant ocurridoEn;
}
