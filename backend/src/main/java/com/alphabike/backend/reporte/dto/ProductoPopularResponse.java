package com.alphabike.backend.reporte.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoPopularResponse {
    private String productoId;
    private String productoNombre;
    private Long totalVendido;
}