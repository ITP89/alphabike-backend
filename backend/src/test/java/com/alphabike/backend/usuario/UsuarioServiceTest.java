package com.alphabike.backend.usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository repository;
    @Mock com.alphabike.backend.pedido.PedidoRepository pedidos;
    @Mock com.alphabike.backend.cita.CitaRepository citas;
    @Mock org.springframework.security.crypto.password.PasswordEncoder passwords;
    @InjectMocks UsuarioService service;
    Usuario admin(String id) { return Usuario.builder().id(id).nombre(id).email(id+"@test.invalid").rol(Usuario.Rol.ADMIN).estado(Usuario.Estado.ACTIVO).build(); }
    @Test void noSePuedeDesactivarNiDegradarAlUltimoAdmin() {
        var ultimo = admin("1");
        when(repository.lockAdministradores()).thenReturn(List.of(ultimo));
        when(repository.findById("1")).thenReturn(Optional.of(ultimo));
        assertThatThrownBy(() -> service.cambiarEstado("1", "INACTIVO")).hasMessageContaining("administrador activo");
        assertThatThrownBy(() -> service.cambiarRol("1", "CLIENTE")).hasMessageContaining("administrador activo");
        verify(repository, never()).save(any());
        assertThat(ultimo.getRol()).isEqualTo(Usuario.Rol.ADMIN);
    }
    @Test void permiteCambiarAccesoSiQuedaOtroAdministradorActivo() {
        var primero = admin("1");
        when(repository.lockAdministradores()).thenReturn(List.of(primero, admin("2")));
        when(repository.findById("1")).thenReturn(Optional.of(primero));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(service.cambiarRol("1", "CLIENTE").getRol()).isEqualTo("CLIENTE");
    }
}
