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
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String referenciaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2, message = "El importe admite hasta 8 enteros y 2 decimales")
    private BigDecimal monto;

    @NotBlank(message = "El metodo de pago es obligatorio")
    @ValidEnum(enumClass = Pago.MetodoPago.class, message = "El metodo de pago no es valido")
    private String metodoPago;

    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String codigoAutorizacion;
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String transaccionId;
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String tarjetaMarca;
    @Size(max = 255, message = "El campo no puede superar 255 caracteres")
    private String tarjetaUltimos4;
}
