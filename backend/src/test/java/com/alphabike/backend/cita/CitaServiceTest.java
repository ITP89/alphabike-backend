package com.alphabike.backend.cita;

import com.alphabike.backend.servicio.Servicio;
import com.alphabike.backend.servicio.ServicioRepository;
import com.alphabike.backend.shared.exception.UnauthorizedException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private CitaService citaService;

    @Test
    void obtenerRechazaClienteQueNoEsDuenio() {
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
        Cita cita = Cita.builder()
                .id("cita-1")
                .cliente(clienteDuenio)
                .servicio(Servicio.builder().id("servicio-1").nombre("Mantenimiento").build())
                .fecha(LocalDate.now())
                .hora(LocalTime.NOON)
                .estado(Cita.Estado.PENDIENTE)
                .build();

        when(citaRepository.findById("cita-1")).thenReturn(Optional.of(cita));
        when(usuarioRepository.findByEmail("cliente1@test.com")).thenReturn(Optional.of(clienteAuth));

        assertThatThrownBy(() -> citaService.obtener("cita-1", "cliente1@test.com"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("No tienes permiso para ver esta cita");
    }
}
