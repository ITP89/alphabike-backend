package com.alphabike.backend.auth;

import com.alphabike.backend.auth.dto.AuthResponse;
import com.alphabike.backend.auth.dto.RegisterRequest;
import com.alphabike.backend.email.EmailService;
import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EmailService emailService;
    @InjectMocks private AuthService authService;

    @Test
    void registroNoEntregaJwtAntesDeVerificarCorreo() {
        RegisterRequest request = new RegisterRequest(
                "Cliente Test", "CLIENTE@TEST.COM", "segura123", "999999999");
        when(usuarioRepository.existsByEmail("cliente@test.com")).thenReturn(false);
        when(passwordEncoder.encode("segura123")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId("cliente-1");
            return usuario;
        });

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isNull();
        assertThat(response.isEmailVerificado()).isFalse();
        assertThat(response.getEmail()).isEqualTo("cliente@test.com");
        verify(jwtTokenProvider, never()).generateToken(any(), any());
    }
}
