package com.alphabike.backend.cotizacion.dto;

import com.alphabike.backend.cotizacion.Cotizacion;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotizacionResponse {

    private String id;
    private String citaId;
    private String descripcion;
    private BigDecimal monto;
    private LocalDateTime fechaEmision;
    private String estado;

    public static CotizacionResponse from(Cotizacion cotizacion) {
        return CotizacionResponse.builder()
                .id(cotizacion.getId())
                .citaId(cotizacion.getCita().getId())
                .descripcion(cotizacion.getDescripcion())
                .monto(cotizacion.getMonto())
                .fechaEmision(cotizacion.getFechaEmision())
                .estado(cotizacion.getEstado().name())
                .build();
    }
}