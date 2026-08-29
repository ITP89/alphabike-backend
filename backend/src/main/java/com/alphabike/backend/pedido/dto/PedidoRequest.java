package com.alphabike.backend.pedido.dto;

import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.shared.validation.ValidEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequest {

    @NotBlank(message = "El tipo de entrega es obligatorio")
    @ValidEnum(enumClass = Pedido.TipoEntrega.class, message = "El tipo de entrega no es valido")
    private String tipoEntrega;

    private String direccionEntrega;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<@Valid DetallePedidoRequest> detalles;
}
