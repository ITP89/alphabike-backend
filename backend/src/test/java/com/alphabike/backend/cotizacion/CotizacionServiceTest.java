package com.alphabike.backend.cotizacion;

import com.alphabike.backend.cita.Cita;
import com.alphabike.backend.cita.CitaRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CotizacionService cotizacionService;

    @Test
    void obtenerRechazaClienteQueNoEsDuenioDeLaCita() {
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
        Cotizacion cotizacion = Cotizacion.builder()
                .id("cotizacion-1")
                .cita(Cita.builder().id("cita-1").cliente(clienteDuenio).build())
                .descripcion("Cambio de transmision")
                .monto(new BigDecimal("120.00"))
                .estado(Cotizacion.Estado.PENDIENTE)
                .build();

        when(cotizacionRepository.findById("cotizacion-1")).thenReturn(Optional.of(cotizacion));
        when(usuarioRepository.findByEmail("cliente1@test.com")).thenReturn(Optional.of(clienteAuth));

        assertThatThrownBy(() -> cotizacionService.obtener("cotizacion-1", "cliente1@test.com"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("No tienes permiso para ver esta cotizacion");
    }
}
