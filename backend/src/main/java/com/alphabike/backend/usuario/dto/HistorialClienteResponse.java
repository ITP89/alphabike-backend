package com.alphabike.backend.usuario.dto;

import com.alphabike.backend.cita.dto.CitaResponse;
import com.alphabike.backend.pedido.dto.PedidoResponse;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialClienteResponse {

    private UsuarioResponse usuario;
    private List<PedidoResponse> pedidos;
    private List<CitaResponse> citas;
}