package com.alphabike.backend.auth;

import com.alphabike.backend.auth.dto.AuthResponse;
import com.alphabike.backend.auth.dto.LoginRequest;
import com.alphabike.backend.auth.dto.RegisterRequest;
import com.alphabike.backend.security.JwtTokenProvider;
import com.alphabike.backend.shared.exception.BadRequestException;
import com.alphabike.backend.shared.exception.ResourceNotFoundException;
import com.alphabike.backend.usuario.Usuario;
import com.alphabike.backend.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("El email ya esta registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(Usuario.Rol.CLIENTE)
                .estado(Usuario.Estado.ACTIVO)
                .build();

        usuarioRepository.save(usuario);
        return toAuthResponse(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadRequestException("Credenciales incorrectas");
        }

        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            throw new BadRequestException("Usuario inactivo");
        }

        return toAuthResponse(usuario);
    }

    public AuthResponse me(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return toAuthResponse(usuario);
    }

    private AuthResponse toAuthResponse(Usuario usuario) {
        String token = jwtTokenProvider.generateToken(usuario.getEmail(), usuario.getRol().name());

        return AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol().name())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
