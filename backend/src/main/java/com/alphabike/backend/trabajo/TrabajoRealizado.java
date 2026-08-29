package com.alphabike.backend.trabajo;

import com.alphabike.backend.cita.Cita;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "trabajos_realizados", schema = "taller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @Column(nullable = false)
    private String titulo;

    @Column
    private String descripcion;

    @Column(name = "imagen_antes_url")
    private String imagenAntesUrl;

    @Column(name = "imagen_despues_url")
    private String imagenDespuesUrl;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Boolean destacado;

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) this.fecha = LocalDate.now();
        if (this.destacado == null) this.destacado = false;
    }
}