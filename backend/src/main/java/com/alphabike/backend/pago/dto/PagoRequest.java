package com.alphabike.backend.pago.dto;

import com.alphabike.backend.pago.Pago;
import com.alphabike.backend.shared.validation.ValidEnum;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequest {

    @NotBlank(message = "El tipo de referencia es obligatorio")
    @ValidEnum(enumClass = Pago.ReferenciaTipo.class, message = "El tipo de referencia no es valido")
    private String referenciaTipo;

    @NotBlank(message = "El id de referencia es obligatorio")
    private String referenciaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monto;

    @NotBlank(message = "El metodo de pago es obligatorio")
    @ValidEnum(enumClass = Pago.MetodoPago.class, message = "El metodo de pago no es valido")
    private String metodoPago;
}
