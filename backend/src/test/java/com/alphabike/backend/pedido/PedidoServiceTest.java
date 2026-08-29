package com.alphabike.backend.pedido;

import com.alphabike.backend.categoria.Categoria;
import com.alphabike.backend.pedido.dto.DetallePedidoRequest;
import com.alphabike.backend.pedido.dto.PedidoRequest;
import com.alphabike.backend.producto.Producto;
import com.alphabike.backend.producto.ProductoRepository;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void crearRechazaPedidoCuandoNoHayStockSuficiente() {
        Usuario cliente = Usuario.builder()
                .id("cliente-1")
                .email("cliente@test.com")
                .nombre("Cliente Test")
                .build();
        Producto producto = Producto.builder()
                .id("producto-1")
                .nombre("Cadena")
                .marca("Shimano")
                .precio(new BigDecimal("30.00"))
                .stock(1)
                .estado(Producto.Estado.ACTIVO)
                .categoria(Categoria.builder().id("cat-1").nombre("Repuestos").build())
                .build();
        PedidoRequest request = new PedidoRequest(
                "RECOJO_TIENDA",
                null,
                List.of(new DetallePedidoRequest("producto-1", 2, null))
        );

        when(usuarioRepository.findByEmail("cliente@test.com")).thenReturn(Optional.of(cliente));
        when(productoRepository.findByIdForUpdate("producto-1")).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> pedidoService.crear(request, "cliente@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(producto.getStock()).isEqualTo(1);
        verify(pedidoRepository, never()).save(any());
        verify(detallePedidoRepository, never()).save(any());
    }

    @Test
    void obtenerLanzaUnauthorizedExceptionSiClienteIntentaVerPedidoDeOtro() {
        Usuario clienteAuth = Usuario.builder()
                .id("cliente-1")
                .email("cliente1@test.com")
                .rol(Usuario.Rol.CLIENTE)
                .build();

        Usuario clienteDuenio = Usuario.builder()
                .id("cliente-2")
                .email("cliente2@test.com")
                .rol(Usuario.Rol.CLIENTE)
                .build();

        Pedido pedido = Pedido.builder()
                .id("pedido-100")
                .cliente(clienteDuenio)
                .build();

        when(pedidoRepository.findById("pedido-100")).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByEmail("cliente1@test.com")).thenReturn(Optional.of(clienteAuth));

        assertThatThrownBy(() -> pedidoService.obtener("pedido-100", "cliente1@test.com"))
                .isInstanceOf(com.alphabike.backend.shared.exception.UnauthorizedException.class)
                .hasMessageContaining("No tiene permiso para ver este pedido");
    }
}
