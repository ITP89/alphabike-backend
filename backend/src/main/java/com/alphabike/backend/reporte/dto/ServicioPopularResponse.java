package com.alphabike.backend.reporte.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioPopularResponse {
    private String servicioId;
    private String servicioNombre;
    private Long totalCitas;
}