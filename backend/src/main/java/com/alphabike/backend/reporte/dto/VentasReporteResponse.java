package com.alphabike.backend.reporte.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentasReporteResponse {

    private BigDecimal ingresosTotales;
    private BigDecimal ingresosPedidos;
    private BigDecimal ingresosServicios;
    private Long totalPedidos;
    private Long totalCitas;
    private BigDecimal totalDescuentos;
}