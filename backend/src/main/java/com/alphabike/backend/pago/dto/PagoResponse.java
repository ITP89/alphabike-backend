package com.alphabike.backend.pago.dto;

import com.alphabike.backend.pago.Pago;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {

    private String id;
    private String referenciaTipo;
    private String referenciaId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fecha;

    public static PagoResponse from(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .referenciaTipo(pago.getReferenciaTipo().name())
                .referenciaId(pago.getReferenciaId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago().name())
                .estado(pago.getEstado().name())
                .fecha(pago.getFecha())
                .build();
    }
}