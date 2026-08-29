package com.alphabike.backend.reporte.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescuentosReporteResponse {

    private String encargadoId;
    private String encargadoNombre;
    private Long totalVentas;
    private BigDecimal totalDescuentos;
    private BigDecimal promedioDescuento;
}