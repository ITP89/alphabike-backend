package com.alphabike.backend.servicio.dto;

import com.alphabike.backend.servicio.Servicio;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioResponse {

    private String id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Integer duracionMin;

    public static ServicioResponse from(Servicio servicio) {
        return ServicioResponse.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precioBase(servicio.getPrecioBase())
                .duracionMin(servicio.getDuracionMin())
                .build();
    }
}