package com.alphabike.backend.pedido.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoRequest {

    @NotBlank(message = "El producto es obligatorio")
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1)
    private Integer cantidad;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio acordado debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El importe admite hasta 8 enteros y 2 decimales")
    private BigDecimal precioAcordado;
}
