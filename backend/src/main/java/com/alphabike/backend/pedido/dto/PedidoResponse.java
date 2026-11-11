package com.alphabike.backend.pedido.dto;

import com.alphabike.backend.pedido.DetallePedido;
import com.alphabike.backend.pedido.Pedido;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponse {

    private String id;
    private String clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private LocalDateTime fecha;
    private String estado;
    private String estadoPago;
    private String tipoEntrega;
    private String direccionEntrega;
    private BigDecimal costoEnvio;
    private String numeroSeguimiento;
    private BigDecimal total;
    private List<DetalleResponse> detalles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetalleResponse {
        private String id;
        private String productoId;
        private String productoNombre;
        private String imagenUrl;
        private Integer cantidad;
        private BigDecimal precioLista;
        private BigDecimal precioAcordado;
        private BigDecimal subtotal;

        public static DetalleResponse from(DetallePedido detalle) {
            return DetalleResponse.builder()
                    .id(detalle.getId())
                    .productoId(detalle.getProducto().getId())
                    .productoNombre(detalle.getProducto().getNombre())
                    .imagenUrl(detalle.getProducto().getImagenUrl())
                    .cantidad(detalle.getCantidad())
                    .precioLista(detalle.getPrecioLista())
                    .precioAcordado(detalle.getPrecioAcordado())
                    .subtotal(detalle.getSubtotal())
                    .build();
        }
    }

    public static PedidoResponse from(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .clienteId(pedido.getCliente().getId())
                .clienteNombre(pedido.getCliente().getNombre())
                .clienteEmail(pedido.getCliente().getEmail())
                .fecha(pedido.getFecha())
                .estado(pedido.getEstado().name())
                .estadoPago(pedido.getEstado() == Pedido.Estado.PENDIENTE || pedido.getEstado() == Pedido.Estado.CANCELADO
                        ? "PENDIENTE" : "PAGADO")
                .tipoEntrega(pedido.getTipoEntrega().name())
                .direccionEntrega(pedido.getDireccionEntrega())
                .costoEnvio(pedido.getCostoEnvio())
                .numeroSeguimiento(pedido.getNumeroSeguimiento())
                .total(pedido.getTotal())
                .detalles(pedido.getDetalles() != null ?
                        pedido.getDetalles().stream()
                                .map(DetalleResponse::from)
                                .toList() : null)
                .build();
    }
}