package com.alphabike.backend.pedido;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ReservaVencidaTest {
    @Mock PedidoRepository pedidos;
    @Mock DetallePedidoRepository detalles;
    @Mock com.alphabike.backend.usuario.UsuarioRepository usuarios;
    @Mock com.alphabike.backend.producto.ProductoRepository productos;
    @InjectMocks PedidoService service;
    @Test void nuncaLiberaUnPagoRemotoEnProcesoNiUnPedidoPagado() {
        var p = Pedido.builder().id("1").estado(Pedido.Estado.PENDIENTE).mercadoPagoOrderId("remote-1")
                .fecha(LocalDateTime.now().minusHours(2)).build();
        when(pedidos.findByIdForUpdate("1")).thenReturn(Optional.of(p));
        assertThat(service.liberarReservaVencida("1", LocalDateTime.now())).isFalse();
        p.setMercadoPagoOrderId(null); p.setEstado(Pedido.Estado.PAGADO);
        assertThat(service.liberarReservaVencida("1", LocalDateTime.now())).isFalse();
        verify(pedidos, never()).save(any());
        verifyNoInteractions(detalles, productos);
    }
}
