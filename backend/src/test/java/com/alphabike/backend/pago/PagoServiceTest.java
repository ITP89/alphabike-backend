package com.alphabike.backend.pago;

import com.alphabike.backend.cotizacion.CotizacionRepository;
import com.alphabike.backend.pago.dto.PagoRequest;
import com.alphabike.backend.pedido.Pedido;
import com.alphabike.backend.pedido.PedidoRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock private PagoRepository pagoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private CotizacionRepository cotizacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private PagoService pagoService;

    @Test
    void rechazaPagoDuplicado() {
        Usuario cliente = cliente("cliente-1", "cliente@test.com");
        PagoRequest request = pagoPedido("pedido-1", "50.00");
        when(usuarioRepository.findByEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));
        when(pagoRepository.existsByReferenciaTipoAndReferenciaIdAndEstado(
                Pago.ReferenciaTipo.PEDIDO, "pedido-1", Pago.Estado.PAGADO)).thenReturn(true);

        assertThatThrownBy(() -> pagoService.registrar(request, cliente.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ya tiene un pago");
        verify(pedidoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaPagoDePedidoAjeno() {
        Usuario autenticado = cliente("cliente-1", "cliente1@test.com");
        Usuario propietario = cliente("cliente-2", "cliente2@test.com");
        Pedido pedido = Pedido.builder()
                .id("pedido-1")
                .cliente(propietario)
                .estado(Pedido.Estado.PENDIENTE)
                .total(new BigDecimal("50.00"))
                .build();
        when(usuarioRepository.findByEmail(autenticado.getEmail())).thenReturn(Optional.of(autenticado));
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagoService.registrar(pagoPedido("pedido-1", "50.00"), autenticado.getEmail()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("permiso");
        verify(pagoRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaMontoDistintoAlTotal() {
        Usuario cliente = cliente("cliente-1", "cliente@test.com");
        Pedido pedido = Pedido.builder()
                .id("pedido-1")
                .cliente(cliente)
                .estado(Pedido.Estado.PENDIENTE)
                .total(new BigDecimal("50.00"))
                .build();
        when(usuarioRepository.findByEmail(cliente.getEmail())).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById("pedido-1")).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagoService.registrar(pagoPedido("pedido-1", "49.99"), cliente.getEmail()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("coincidir exactamente");
    }

    private Usuario cliente(String id, String email) {
        return Usuario.builder().id(id).email(email).rol(Usuario.Rol.CLIENTE).build();
    }

    private PagoRequest pagoPedido(String pedidoId, String monto) {
        return new PagoRequest("PEDIDO", pedidoId, new BigDecimal(monto), "TARJETA",
                "AUTH-1", "TRX-1", "VISA", "4242");
    }
}
