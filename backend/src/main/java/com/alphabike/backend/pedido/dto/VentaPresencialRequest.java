package com.alphabike.backend.pedido.dto;

import com.alphabike.backend.pago.Pago;
import com.alphabike.backend.shared.validation.ValidEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaPresencialRequest {
    @NotBlank
    @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", message = "El identificador de operacion debe ser UUID")
    private String operacionId;

    @NotEmpty
    private List<@NotNull @Valid DetallePedidoRequest> detalles;

    @NotBlank
    @ValidEnum(enumClass = Pago.MetodoPago.class)
    private String metodoPago;
}
