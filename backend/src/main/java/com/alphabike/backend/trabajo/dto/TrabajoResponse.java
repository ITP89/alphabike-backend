package com.alphabike.backend.trabajo.dto;

import com.alphabike.backend.trabajo.TrabajoRealizado;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoResponse {

    private String id;
    private String citaId;
    private String titulo;
    private String descripcion;
    private String imagenAntesUrl;
    private String imagenDespuesUrl;
    private LocalDate fecha;
    private Boolean destacado;

    public static TrabajoResponse from(TrabajoRealizado trabajo) {
        return TrabajoResponse.builder()
                .id(trabajo.getId())
                .citaId(trabajo.getCita() != null ? trabajo.getCita().getId() : null)
                .titulo(trabajo.getTitulo())
                .descripcion(trabajo.getDescripcion())
                .imagenAntesUrl(trabajo.getImagenAntesUrl())
                .imagenDespuesUrl(trabajo.getImagenDespuesUrl())
                .fecha(trabajo.getFecha())
                .destacado(trabajo.getDestacado())
                .build();
    }
}