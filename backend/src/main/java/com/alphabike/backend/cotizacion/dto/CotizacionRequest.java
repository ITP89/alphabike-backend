package com.alphabike.backend.cotizacion.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionRequest {

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2, message = "El importe admite hasta 8 enteros y 2 decimales")
    private BigDecimal monto;
}