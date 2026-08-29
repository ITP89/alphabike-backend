package com.alphabike.backend.servicio.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal precioBase;

    @NotNull(message = "La duracion es obligatoria")
    @Min(value = 1)
    private Integer duracionMin;
}